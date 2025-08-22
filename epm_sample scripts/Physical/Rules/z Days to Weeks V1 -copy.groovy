
<HBRRepo>
  <variables/>
  <rulesets/>
  <rules>
    <rule id="1" name="z Days to Weeks V1 -copy" product="Planning">
      <property name="application">RPEPM</property>
      <property name="plantype">Physical</property>
      <script type="groovy">
/* Rule Name: z Days to Weeks V1 - copy
   Purpose  : Converts daily data into weekly ranges and performs cube calculation.
   RTPS     : None
*/

// === Initialize calendar with trial date ===
Calendar calendar = new GregorianCalendar()
SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy")
Date trialTime = sdf.parse("01/08/2024")  // Trial date
calendar.setTime(trialTime)
calendar.setFirstDayOfWeek(Calendar.MONDAY) // Weeks start on Monday
calendar.setMinimalDaysInFirstWeek(7)       // Require full week to be week 1

// === Month names for formatting ===
def monthNames = ["Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"]

// === Compute current and prior week information ===
int currentMonthIndex = calendar.get(Calendar.MONTH)
int currentWeekNum = calendar.get(Calendar.WEEK_OF_YEAR)
int priorWeekNum = currentWeekNum - 1
int currentWeekYear = calendar.get(Calendar.YEAR)
String currentWeekFY = "FY" + (currentWeekYear % 100)
String currentWeekLabel = "Week ${currentWeekNum}"
String priorWeekLabel = "Week ${priorWeekNum}"

// === Weekly rolling map (week number → list of day tuples) ===
Calendar weekCal = new GregorianCalendar()
weekCal.setTime(trialTime)
weekCal.set(Calendar.DAY_OF_MONTH, 1)
weekCal.setFirstDayOfWeek(Calendar.MONDAY)

// Roll back to the Monday on or before the 1st of the month
int dow = weekCal.get(Calendar.DAY_OF_WEEK)
int daysToBackUp = (dow == Calendar.MONDAY) ? 0 : ((dow + 5) % 7)
weekCal.add(Calendar.DAY_OF_MONTH, -daysToBackUp)

// Map to hold week number → daily tuples [Day, Month, FY]
Map<Integer, List<List<String>>> weekToDays = [:]

// === Loop through weeks of the month ===
while (true) {
    List<List<String>> weekTuples = []

    // Collect 7 days for the week
    for (int i = 0; i < 7; i++) {
        int yr = weekCal.get(Calendar.YEAR)
        int mo = weekCal.get(Calendar.MONTH)
        int dom = weekCal.get(Calendar.DAY_OF_MONTH)

        String day = "Day ${String.format('%02d', dom)}"  // Format day with leading zero
        String period = monthNames[mo].toUpperCase()
        String fy = "FY" + (yr % 100)

        weekTuples << [day, period, fy]  // Add day tuple to week
        weekCal.add(Calendar.DAY_OF_MONTH, 1) // Advance to next day
    }

    // Determine the week number for this set of days
    Calendar labelCal = (Calendar) weekCal.clone()
    labelCal.add(Calendar.DAY_OF_MONTH, -7)
    int realWeekNum = labelCal.get(Calendar.WEEK_OF_YEAR)

    weekToDays[realWeekNum] = weekTuples

    // Exit loop if we've moved to next month past the first week
    if (weekCal.get(Calendar.MONTH) != currentMonthIndex &&
        weekCal.get(Calendar.DAY_OF_MONTH) > 7) {
        break
    }
}

// === Build string outputs for current and prior week ===
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

// === Convert StringBuilder to final strings ===
String currentWeekOutput = currentWeekOutputBuilder.toString()
String priorWeekOutput = priorWeekOutputBuilder.toString()

// === Diagnostic output for verification ===
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

// === Split current week string into label and day values for cube calculation ===
def parts = currentWeekOutput.split("=", 2)
def strWeek = parts[0].trim()
def strDayInWeek = parts[1].trim()
println(strWeek)
println(strDayInWeek)

// === Setup cube and calculation parameters ===
Cube cube = operation.getApplication().getCube('Physical')
CustomCalcParameters calcParameters = new CustomCalcParameters()

// === Define POV for cube calculation ===
def povData = [
    ['[Actual]'],
    ['[Final]'],
    ['[Load]'],
    ['[Month]'],
    ['Descendants([Asset Meters]','[Asset Meters].dimension.Levels(0))'],
    ['Descendants([Mining_Equipment]','[Mining_Equipment].dimension.Levels(0))'],
    ['[PRJ_100302]'],
    ['[ENT_41105]']
]
calcParameters.pov = getCrossJoins(povData)

// === Define source region for calculation ===
def crossData = [['[Day 29]','[Day 30]'],['[APR]'],['[FY25]']]
calcParameters.sourceRegion = getCrossJoins(crossData)

// === Script to aggregate daily data into a week ===
String CostCodeBudget = """
    ([Week 18],[BegBalance],[FY25]) := ([Day 29],[APR],[FY25]) + ([Day 30],[APR],[FY25]);
"""
calcParameters.script = CostCodeBudget
calcParameters.roundDigits = 2
cube.executeAsoCustomCalculation(calcParameters)

// === Helper function to generate nested CrossJoin strings dynamically ===
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
</HBRRepo>
