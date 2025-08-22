
<HBRRepo>
  <variables/>
  <rulesets/>
  <rules>
    <rule id="1" name="Physicals - Week Dates_V1" product="Planning">
      <property name="application">RPEPM</property>
      <property name="plantype">Physical</property>
      <script type="groovy">
/* RTPS: */

// === Set calendar to a fixed "trial" date for testing ===
Calendar calendar = new GregorianCalendar()
// Context: Hardcoded trial date for reproducible output (replace with dynamic `new Date()` in prod)
// Use Case: Allows testing historical or future periods consistently
Date trialTime = new Date().parse('dd/MM/yyyy', '01/08/XXXX')  
calendar.setTime(trialTime)
calendar.setFirstDayOfWeek(Calendar.MONDAY)
calendar.setMinimalDaysInFirstWeek(7)

// === Define month names for labeling ===
def monthNames = [
    "Jan", "Feb", "Mar", "Apr", "May", "Jun",
    "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
]

int currentMonthIndex = calendar.get(Calendar.MONTH)
int currentWeekNum = calendar.get(Calendar.WEEK_OF_YEAR)
int priorWeekNum = currentWeekNum - 1
int currentWeekYear = calendar.get(Calendar.YEAR)
String currentWeekFY = "FY" + (currentWeekYear % 100)
String currentWeekLabel = "Week ${currentWeekNum}"
String priorWeekLabel = "Week ${priorWeekNum}"

// === Build rolling week-to-day mapping for current month ===
Calendar weekCal = new GregorianCalendar()
weekCal.setTime(trialTime)
weekCal.set(Calendar.DAY_OF_MONTH, 1)
weekCal.setFirstDayOfWeek(Calendar.MONDAY)

// Context: Roll back to nearest Monday from the 1st of the month
int dow = weekCal.get(Calendar.DAY_OF_WEEK)
int daysToBackUp = (dow == Calendar.MONDAY) ? 0 : ((dow + 5) % 7)
weekCal.add(Calendar.DAY_OF_MONTH, -daysToBackUp)

Map&lt;Integer, List&lt;List&lt;String>>> weekToDays = [:]

// Context: Populate mapping of Week → [Day, Period, Fiscal Year]
while (true) {
    List&lt;List&lt;String>> weekTuples = []

    for (int i = 0; i &lt; 7; i++) {
        int yr = weekCal.get(Calendar.YEAR)
        int mo = weekCal.get(Calendar.MONTH)
        int dom = weekCal.get(Calendar.DAY_OF_MONTH)

        String day = "Day ${String.format('%02d', dom)}"
        String period = monthNames[mo].toUpperCase()
        String fy = "FY" + (yr % 100)

        weekTuples &lt;&lt; [day, period, fy]
        weekCal.add(Calendar.DAY_OF_MONTH, 1)
    }

    Calendar labelCal = (Calendar) weekCal.clone()
    labelCal.add(Calendar.DAY_OF_MONTH, -7)
    int realWeekNum = labelCal.get(Calendar.WEEK_OF_YEAR)

    weekToDays[realWeekNum] = weekTuples

    // Stop once we move beyond current month + safety buffer
    if (weekCal.get(Calendar.MONTH) != currentMonthIndex &amp;&amp;
        weekCal.get(Calendar.DAY_OF_MONTH) > 7) {
        break
    }
}

// === Build separate outputs for current and prior week ===
StringBuilder currentWeekOutputBuilder = new StringBuilder()
StringBuilder priorWeekOutputBuilder = new StringBuilder()

if (weekToDays.containsKey(currentWeekNum)) {
    currentWeekOutputBuilder &lt;&lt; "${currentWeekLabel} = \n"
    List&lt;List&lt;String>> entries = weekToDays[currentWeekNum]
    entries.eachWithIndex { List&lt;String> line, int i ->
        String suffix = (i &lt; entries.size() - 1) ? "+" : ""
        currentWeekOutputBuilder &lt;&lt; "[${line[0]}],[${line[1]}],[${line[2]}]${suffix}\n"
    }
}

if (weekToDays.containsKey(priorWeekNum)) {
    priorWeekOutputBuilder &lt;&lt; "${priorWeekLabel} = \n"
    List&lt;List&lt;String>> entries = weekToDays[priorWeekNum]
    entries.eachWithIndex { List&lt;String> line, int i ->
        String suffix = (i &lt; entries.size() - 1) ? "+" : ""
        priorWeekOutputBuilder &lt;&lt; "[${line[0]}],[${line[1]}],[${line[2]}]${suffix}\n"
    }
}

// === Final string results for diagnostics ===
String currentWeekOutput = currentWeekOutputBuilder.toString()
String priorWeekOutput = priorWeekOutputBuilder.toString()

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

// Context: Split the week output into Week name vs. Day list
def parts = currentWeekOutput.split("=", 2)
def strWeek = parts[0].trim()
def strDayInWeek = parts[1].trim()

println(strWeek)
println(strDayInWeek)

// === Execute ASO cube calculation ===
Cube cube = operation.getApplication().getCube('Physical')
CustomCalcParameters calcParameters = new CustomCalcParameters()

// === POV definition ===
// Context: Adjusted to avoid exposing real project/entity codes
def povData = [
    ['[Actual]'],
    ['[Final]'],
    ['[Load]'],
    ['[Month]'],
    ['Descendants([Asset Group]','[Asset Group].dimension.Levels(0))'],
    ['Descendants([Equipment]','[Equipment].dimension.Levels(0))'],
    ['[PRJ_xxxxxx]'],    // Use Case: Placeholder project
    ['[ENT_xxxxx]']      // Use Case: Placeholder entity
]

calcParameters.pov = getCrossJoins(povData)

// === Source region for mapping days to a target week ===
// Context: Example maps Day 29 + Day 30 of April into Week 18 FY25
def crossData = [['[Day 29]','[Day 30]'],['[APR]'],['[FYxx]']]
calcParameters.sourceRegion = getCrossJoins(crossData)

// Example calculation rule
String CostCodeBudget = """
    ([Week 18],[BegBalance],[FYxx]) := ([Day 29],[APR],[FYxx]) + ([Day 30],[APR],[FYxx]); 
"""

// Run the calculation script
calcParameters.script = CostCodeBudget
calcParameters.roundDigits = 2
cube.executeAsoCustomCalculation(calcParameters)

// === Helper to generate nested CrossJoin POVs ===
def getCrossJoins(List&lt;List&lt;String>> essIntersection) {
    String crossJoinString
    if (essIntersection.size() > 1) {
        // Context: Inject nested CrossJoin format from innermost to outermost
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
    <deployobject product="2" application="rpepm" plantype="physical" obj_id="1" obj_type="1" name="PHYSICALS - WEEK DATES_V1"/>
  </deployobjects>
</HBRRepo>
