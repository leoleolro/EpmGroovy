
<HBRRepo>
  <variables/>
  <rulesets/>
  <rules>
    <rule id="1" name="z Days to Weeks V2" product="Planning">
      <property name="application">RPEPM</property>
      <property name="plantype">Physical</property>
      <script type="groovy">
/* RTPS:
   Context: This Groovy rule converts daily data into weekly aggregated values for a given trial date.
   Purpose: Prepares dynamic CrossJoin inputs and executes a cube calculation to assign daily data into the weekly bucket.
*/

// === Set calendar to the trial date ===
Calendar calendar = new GregorianCalendar()
SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy")
Date trialTime = sdf.parse("01/08/2024")  // Trial date to base week calculations
calendar.setTime(trialTime)
calendar.setFirstDayOfWeek(Calendar.MONDAY) // Week starts on Monday
calendar.setMinimalDaysInFirstWeek(7)       // Require full week to be considered week 1

// === Define month names for label formatting ===
def monthNames = ["Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"]

// === Compute current and prior week details ===
int currentMonthIndex = calendar.get(Calendar.MONTH)
int currentWeekNum = calendar.get(Calendar.WEEK_OF_YEAR)
int priorWeekNum = currentWeekNum - 1
int currentWeekYear = calendar.get(Calendar.YEAR)
String currentWeekFY = "FY" + (currentWeekYear % 100)
String currentWeekLabel = "Week ${currentWeekNum}"
String priorWeekLabel = "Week ${priorWeekNum}"

// === Initialize weekly rolling calendar ===
Calendar weekCal = new GregorianCalendar()
weekCal.setTime(trialTime)
weekCal.set(Calendar.DAY_OF_MONTH, 1)
weekCal.setFirstDayOfWeek(Calendar.MONDAY)

// Roll back to the Monday on or before the 1st of the month
int dow = weekCal.get(Calendar.DAY_OF_WEEK)
int daysToBackUp = (dow == Calendar.MONDAY) ? 0 : ((dow + 5) % 7)
weekCal.add(Calendar.DAY_OF_MONTH, -daysToBackUp)

// === Map to store week number → list of day tuples ===
Map<Integer, List<List<String>>> weekToDays = [:]
def dayOnlyList 
def monthOnlyList 
def yearOnlyList 

// === Loop through all weeks in the month and store daily data ===
while (true) {
    List<List<String>> weekTuples = []

    for (int i = 0; i < 7; i++) { 
        int yr = weekCal.get(Calendar.YEAR)
        int mo = weekCal.get(Calendar.MONTH)
        int dom = weekCal.get(Calendar.DAY_OF_MONTH)

        // Format daily label, month label, and fiscal year
        String day = "Day ${dom}"
        String period = monthNames[mo].toUpperCase()
        String fy = "FY" + (yr % 100)

        weekTuples << [day, period, fy]  // Append day info for the week
        weekCal.add(Calendar.DAY_OF_MONTH, 1) // Move to next day
    }

    // Determine actual week number for the week
    Calendar labelCal = (Calendar) weekCal.clone()
    labelCal.add(Calendar.DAY_OF_MONTH, -7)
    int realWeekNum = labelCal.get(Calendar.WEEK_OF_YEAR)

    weekToDays[realWeekNum] = weekTuples

    // Stop if we move to next month past the first week
    if (weekCal.get(Calendar.MONTH) != currentMonthIndex &&
        weekCal.get(Calendar.DAY_OF_MONTH) > 7) {
        break
    }
}

// === Build string outputs for current and prior weeks ===
StringBuilder currentWeekOutputBuilder = new StringBuilder()
StringBuilder priorWeekOutputBuilder = new StringBuilder()

// Build current week outputs and prepare day/month/year lists for CrossJoin
if (weekToDays.containsKey(currentWeekNum)) {
    currentWeekOutputBuilder << "${currentWeekLabel} = \n"
    List<List<String>> entries = weekToDays[currentWeekNum]
    
    // Prepare comma-separated lists for cube CrossJoin
    dayOnlyList = entries.collect { "[${it[0]}]" }.join(',')
    println("dayOnlyList:- " + dayOnlyList)
    
    monthOnlyList = entries.collect { "[${it[1]}]" }.unique().join(',')
    println("monthOnlyList:- " + monthOnlyList)
    
    yearOnlyList = entries.collect { "[${it[2]}]" }.unique().join(',')
    println("yearOnlyList:- " + yearOnlyList)
    
    // Build formatted current week string with '+' separator
    entries.eachWithIndex { List<String> line, int i ->
        String suffix = (i < entries.size() - 1) ? "+" : ""
        currentWeekOutputBuilder << "[${line[0]}],[${line[1]}],[${line[2]}]${suffix}\n"
    }
}

// Build prior week string for reference
if (weekToDays.containsKey(priorWeekNum)) {
    priorWeekOutputBuilder << "${priorWeekLabel} = \n"
    List<List<String>> entries = weekToDays[priorWeekNum]
    entries.eachWithIndex { List<String> line, int i ->
        String suffix = (i < entries.size() - 1) ? "+" : ""
        priorWeekOutputBuilder << "[${line[0]}],[${line[1]}],[${line[2]}]${suffix}\n"
    }
}

// === Final string outputs ===
String currentWeekOutput = currentWeekOutputBuilder.toString()
String priorWeekOutput = priorWeekOutputBuilder.toString()

// === Diagnostic output ===
println "=== Diagnostic Output ==="
println "Trial Date: $trialTime"
println "Current Week Number: $currentWeekNum"
println "Current Week Label: $currentWeekLabel"
println "Current Week Fiscal Year: $currentWeekFY"
println "\n=== Current Week Output ===\n$currentWeekOutput"
println "\n=== Prior Week Output ===\n$priorWeekOutput"

// === Prepare daily assignment string for ASO calculation ===
def parts1 = currentWeekOutput.split("=", 2)
def dataLines = parts1[1].toString().trim()

// Remove '+' and split into individual lines
def lines = dataLines.replaceAll("\\+", "").split("\n").collect { it.toString().trim() }

// Wrap each line in parentheses for aggregation
def daysDataToAssign = lines.collect { "(${it})" }.join(" + ")
println("daysDataToAssign:-" + daysDataToAssign)

// === Initialize cube and calculation parameters ===
Cube cube = operation.getApplication().getCube('Physical')
CustomCalcParameters calcParameters = new CustomCalcParameters()

// === Create POV for calculation ===
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
def crossData = [[dayOnlyList],[monthOnlyList],[yearOnlyList]]
calcParameters.sourceRegion = getCrossJoins(crossData)

// === Script to assign aggregated daily data to week in cube ===
String strWeekAssignment = """
    ([${currentWeekLabel}],[BegBalance],[${currentWeekFY}]) := ${daysDataToAssign};
"""
calcParameters.script = strWeekAssignment
calcParameters.roundDigits = 2

// Execute the ASO calculation
cube.executeAsoCustomCalculation(calcParameters)

// === Helper function to build nested CrossJoin strings ===
def getCrossJoins(List<List<String>> essIntersection) {
    String crossJoinString
    if (essIntersection.size() > 1) {
        // Build nested CrossJoin from innermost to outermost
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
    <deployobject product="2" application="rpepm" plantype="physical" obj_id="1" obj_type="1" name="Z DAYS TO WEEKS V2"/>
  </deployobjects>
</HBRRepo>
