
<HBRRepo>
  <variables/>
  <rulesets/>
  <rules>
    <rule id="1" name="z Days to Weeks V3" product="Planning">
      <property name="application">RPEPM</property>
      <property name="plantype">Physical</property>
      <script type="groovy">
/* RTPS:
   Context: This version converts daily data into weekly aggregates starting from a specific trial date.
   Purpose: Prepares weekly POV and sourceRegion for cube calculation, assigning daily data to the current week dynamically.
*/

// === Set calendar to the trial date ===
Calendar calendar = new GregorianCalendar()
Date trialTime = new Date().parse('dd/MM/yyyy', '31/01/2025')  // Base date for week calculation
calendar.setTime(trialTime)
calendar.setFirstDayOfWeek(Calendar.MONDAY)  // Week starts on Monday
calendar.setMinimalDaysInFirstWeek(7)        // Full week counts as week 1

// === Define month names for labeling ===
List<String> monthNames = ["Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"]

// === Compute current and prior week information ===
int currentWeekNum = calendar.get(Calendar.WEEK_OF_YEAR)
int priorWeekNum = currentWeekNum - 1
int currentWeekYear = calendar.get(Calendar.YEAR)
String currentWeekFY = "FY" + (currentWeekYear % 100)
String currentWeekLabel = "Week ${currentWeekNum}"
String priorWeekLabel = "Week ${priorWeekNum}"

// === Output storage ===
StringBuilder currentWeekOutputBuilder = new StringBuilder()
StringBuilder priorWeekOutputBuilder = new StringBuilder()

// === Helper closure to collect 7 days starting from a given Monday ===
Closure<List<List<Serializable>>> collectWeek = { Calendar startCal ->
    List<List<Serializable>> result = []
    Calendar temp = (Calendar) startCal.clone()
    for (int i = 0; i < 7; i++) {
        int yr = temp.get(Calendar.YEAR)
        int mo = temp.get(Calendar.MONTH)
        int dom = temp.get(Calendar.DAY_OF_MONTH)
        String day = "Day ${dom}"             // Format day label
        String period = monthNames.get(mo).toUpperCase()  // Month label
        String fy = "FY" + (yr % 100)        // Fiscal year label

        result.add([day, period, fy] as List<Serializable>)
        temp.add(Calendar.DAY_OF_MONTH, 1)    // Move to next day
    }
    return result
}

// === Align calendar to start of the week (Monday) ===
Calendar alignedCal = (Calendar) calendar.clone()
int dow = alignedCal.get(Calendar.DAY_OF_WEEK)
int daysToBackUp = (dow == Calendar.MONDAY) ? 0 : ((dow + 5) % 7)
alignedCal.add(Calendar.DAY_OF_MONTH, -daysToBackUp)

// === Get prior week calendar ===
Calendar priorWeekCal = (Calendar) alignedCal.clone()
priorWeekCal.add(Calendar.DAY_OF_MONTH, -7)

// === Collect days for current and prior week ===
List<List<Serializable>> priorWeekDays = collectWeek(priorWeekCal)
List<List<Serializable>> currentWeekDays = collectWeek(alignedCal)

// === Build output strings for current and prior weeks ===
priorWeekOutputBuilder.append("${priorWeekLabel} = \n")
for (int i = 0; i < priorWeekDays.size(); i++) {
    List<Serializable> line = priorWeekDays.get(i)
    String suffix = (i < priorWeekDays.size() - 1) ? "+" : ""
    priorWeekOutputBuilder.append("[${line[0]}],[${line[1]}],[${line[2]}]${suffix}\n")
}

currentWeekOutputBuilder.append("${currentWeekLabel} = \n")
for (int i = 0; i < currentWeekDays.size(); i++) {
    List<Serializable> line = currentWeekDays.get(i)
    String suffix = (i < currentWeekDays.size() - 1) ? "+" : ""
    currentWeekOutputBuilder.append("[${line[0]}],[${line[1]}],[${line[2]}]${suffix}\n")
}

// === Prepare comma-separated day/month/year lists for cube sourceRegion ===
def dayOnlyList = currentWeekDays.collect { "[${it[0]}]" }.join(',')
def monthOnlyList = currentWeekDays.collect { "[${it[1]}]" }.unique().join(',')
def yearOnlyList = currentWeekDays.collect { "[${it[2]}]" }.unique().join(',')

println("dayOnlyList:- " + dayOnlyList)
println("monthOnlyList:- " + monthOnlyList)
println("yearOnlyList:- " + yearOnlyList)

// === Final string results for debugging ===
String currentWeekOutput = currentWeekOutputBuilder.toString()
String priorWeekOutput = priorWeekOutputBuilder.toString()

println "=== Diagnostic Output ==="
println "Trial Date: $trialTime"
println "Current Week Number: $currentWeekNum"
println "Current Week Label: $currentWeekLabel"
println "Current Week Fiscal Year: $currentWeekFY"
println "Prior Week Number: $priorWeekNum"
println "Prior Week Label: $priorWeekLabel"
println "\n=== Current Week Output ===\n$currentWeekOutput"
println "\n=== Prior Week Output ===\n$priorWeekOutput"

// === Format days/month/year for RHS of cube calculation ===
def parts1 = currentWeekOutput.split("=", 2)
def dataLines = parts1[1].toString().trim()
def lines = dataLines.replaceAll("\\+", "").split("\n").collect { it.toString().trim() }
def daysDataToAssign = lines.collect { "(${it})" }.join(" + ")
println("daysDataToAssign:-" + daysDataToAssign)

// === Example cube calculation usage ===
Cube cube = operation.getApplication().getCube('Physical')
CustomCalcParameters calcParameters = new CustomCalcParameters()

// === POV setup for cube calculation ===
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

// === SourceRegion using day/month/year lists ===
def crossData = [[dayOnlyList], [monthOnlyList], [yearOnlyList]]
calcParameters.sourceRegion = getCrossJoins(crossData)

// === Script to assign daily data to current week ===
String CostCodeBudget = """
([${currentWeekLabel}],[BegBalance],[${currentWeekFY}]) := ${daysDataToAssign};
"""
calcParameters.script = CostCodeBudget
calcParameters.roundDigits = 2

// Execute ASO cube calculation
cube.executeAsoCustomCalculation(calcParameters)

// === Helper to generate nested CrossJoin strings for POV or sourceRegion ===
def getCrossJoins(List<List<String>> essIntersection) {
    String crossJoinString
    if (essIntersection.size() > 1) {
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
    <deployobject product="2" application="rpepm" plantype="physical" obj_id="1" obj_type="1" name="Z DAYS TO WEEKS V3"/>
  </deployobjects>
</HBRRepo>
