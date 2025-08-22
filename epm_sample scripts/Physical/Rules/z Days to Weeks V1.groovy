
<HBRRepo>
  <variables/>
  <rulesets/>
  <rules>
    <rule id="1" name="z Days to Weeks V1" product="Planning">
      <property name="application">RPEPM</property>
      <property name="plantype">Physical</property>
      <script type="groovy">
/* Rule Name: z Days to Weeks V1
   Purpose  : Converts daily data into weekly ranges and performs cube calculation.
   RTPS     : None
*/

// === Set calendar to the trial date ===
Calendar calendar = new GregorianCalendar()
SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy")
Date trialTime = sdf.parse("01/08/2024") // Example fixed trial date: 1st August 2024
calendar.setTime(trialTime)
calendar.setFirstDayOfWeek(Calendar.MONDAY) // Weeks start on Monday
calendar.setMinimalDaysInFirstWeek(7)       // First week must have 7 days

// === Define month names ===
def monthNames = [
    "Jan", "Feb", "Mar", "Apr", "May", "Jun",
    "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
]

// === Determine current/prior week info ===
int currentMonthIndex = calendar.get(Calendar.MONTH)
int currentWeekNum = calendar.get(Calendar.WEEK_OF_YEAR)      // Week number of trial date
int priorWeekNum = currentWeekNum - 1                          // Previous week number
int currentWeekYear = calendar.get(Calendar.YEAR)
String currentWeekFY = "FY" + (currentWeekYear % 100)        // Fiscal year shorthand
String currentWeekLabel = "Week ${currentWeekNum}"
String priorWeekLabel = "Week ${priorWeekNum}"

// === Weekly rolling map preparation ===
Calendar weekCal = new GregorianCalendar()
weekCal.setTime(trialTime)
weekCal.set(Calendar.DAY_OF_MONTH, 1) // Start from the 1st of the month
weekCal.setFirstDayOfWeek(Calendar.MONDAY)

// Roll back to the Monday on or before the 1st
int dow = weekCal.get(Calendar.DAY_OF_WEEK)
int daysToBackUp = (dow == Calendar.MONDAY) ? 0 : ((dow + 5) % 7)
weekCal.add(Calendar.DAY_OF_MONTH, -daysToBackUp)

// Map to hold week number → list of daily tuples [Day, Month, FY]
Map<Integer, List<List<String>>> weekToDays = [:]

// === Populate the weekly map ===
while (true) {
    List<List<String>> weekTuples = []

    for (int i = 0; i < 7; i++) { // Collect 7 days for the week
        int yr = weekCal.get(Calendar.YEAR)
        int mo = weekCal.get(Calendar.MONTH)
        int dom = weekCal.get(Calendar.DAY_OF_MONTH)

        String day = "Day ${String.format('%02d', dom)}" // Format day with leading zero
        String period = monthNames[mo].toUpperCase()    // Month abbreviation
        String fy = "FY" + (yr % 100)                   // Fiscal year shorthand

        weekTuples << [day, period, fy]                // Add tuple to week list
        weekCal.add(Calendar.DAY_OF_MONTH, 1)          // Move to next day
    }

    Calendar labelCal = (Calendar) weekCal.clone()
    labelCal.add(Calendar.DAY_OF_MONTH, -7)
    int realWeekNum = labelCal.get(Calendar.WEEK_OF_YEAR)
    weekToDays[realWeekNum] = weekTuples

    // Stop loop when rolling into the next month past the first week
    if (weekCal.get(Calendar.MONTH) != currentMonthIndex &&
        weekCal.get(Calendar.DAY_OF_MONTH) > 7) {
        break
    }
}

// === Build outputs for current and prior weeks ===
StringBuilder currentWeekOutputBuilder = new StringBuilder()
StringBuilder priorWeekOutputBuilder = new StringBuilder()

if (weekToDays.containsKey(currentWeekNum)) {
    currentWeekOutputBuilder << "${currentWeekLabel} = \n"
    List<List<String>> entries = weekToDays[currentWeekNum]
    entries.eachWithIndex { List<String> line, int i ->
        String suffix = (i < entries.size() - 1) ? "+" : ""
        currentWeekOutputBuilder << "[${line[0]}],[${line[1]}],[${line[2]}]${suffix}\n"
    }
}

if (weekToDays.containsKey(priorWeekNum)) {
    priorWeekOutputBuilder << "${priorWeekLabel} = \n"
    List<List<String>> entries = weekToDays[priorWeekNum]
    entries.eachWithIndex { List<String> line, int i ->
        String suffix = (i < entries.size() - 1) ? "+" : ""
        priorWeekOutputBuilder << "[${line[0]}],[${line[1]}],[${line[2]}]${suffix}\n"
    }
}

// === Convert builders to strings ===
String currentWeekOutput = currentWeekOutputBuilder.toString()
String priorWeekOutput = priorWeekOutputBuilder.toString()

// === Diagnostic output ===
println "=== Diagnostic Output ==="
println "Trial Date: $trialTime"
println "Current Week Number: $currentWeekNum"
println "Current Week Label: $currentWeekLabel"
println "Current Week Year: $currentWeekYear"
println "Current Week Fiscal Year: $currentWeekFY"
println "Prior Week Number: $priorWeekNum"
println "Prior Week Label: $priorWeekLabel"
println "\n=== Current Week Output ===\n$currentWeekOutput"
println "\n=== Prior Week Output ===\n$priorWeekOutput"

// === Split current week output into label and day values ===
def parts = currentWeekOutput.split("=", 2)
def strWeek = parts[0].trim()
def strDayInWeek = parts[1].trim()

println(strWeek)
println(strDayInWeek)

// === Cube calculation setup ===
Cube cube = operation.getApplication().getCube('Physical')
CustomCalcParameters calcParameters = new CustomCalcParameters()

// === Define POV (Point of View) for calculation ===
def povData = [
    ['[Actual]'],
    ['[Final]'],
    ['[Load]'],
    ['[Month]'],
    ['Descendants([Asset Meters]', '[Asset Meters].dimension.Levels(0))'],
    ['Descendants([Mining_Equipment]', '[Mining_Equipment].dimension.Levels(0))'],
    ['[PRJ_100302]'],
    ['[ENT_41105]']
]
calcParameters.pov = getCrossJoins(povData)

// === Define the source region for calculation ===
def crossData = [['[Day 29]','[Day 30]'],['[APR]'],['[FY25]']]
calcParameters.sourceRegion = getCrossJoins(crossData)

// === Calculation script: aggregate daily values into a week ===
String CostCodeBudget = """
    ([Week 18],[BegBalance],[FY25]) := ([Day 29],[APR],[FY25]) + ([Day 30],[APR],[FY25]);
"""
calcParameters.script = CostCodeBudget
calcParameters.roundDigits = 2
cube.executeAsoCustomCalculation(calcParameters)

// === Helper function to generate nested CrossJoins ===
def getCrossJoins(List<List<String>> essIntersection) {
    String crossJoinString
    if (essIntersection.size() > 1) {
        // Create nested CrossJoin from innermost to outermost
        crossJoinString = essIntersection[1..-1].inject('{' + essIntersection[0].join(',') + '}') { concat, members ->
            "CrossJoin(" + concat + ',{' + members.join(',') + '})'
        }
    }
    return crossJoinString
}
      </script>
    </rule>
  </rules>
  <components/>
  <deployobjects>
    <deployobject product="2" application="rpepm" plantype="physical" obj_id="1" obj_type="1" name="Z DAYS TO WEEKS V1"/>
  </deployobjects>
</HBRRepo>
