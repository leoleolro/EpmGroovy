
<HBRRepo>
  <variables/>
  <rulesets/>
  <rules>
    <rule id="1" name="z Days to Weeks" product="Planning">
      <property name="application">RPEPM</property>
      <property name="plantype">Physical</property>
      <script type="groovy">
/* Rule Name : z Days to Weeks
   Purpose   : Converts daily dates into weekly ranges and outputs week labels with corresponding days, month, and fiscal year.
   RTPS      : None
*/

// === Set calendar to the trial date ===
// Creates a Calendar object and sets a fixed trial date (5th Jan 2025)
Calendar calendar = new GregorianCalendar()
Date trialTime = new Date().parse('dd/MM/yyyy', '05/01/2025')
calendar.setTime(trialTime)
calendar.setFirstDayOfWeek(Calendar.MONDAY) // Week starts on Monday
calendar.setMinimalDaysInFirstWeek(7)       // Defines first week as a full week

// === Define month names ===
// List of month abbreviations for use in outputs
List<String> monthNames = [
    "Jan", "Feb", "Mar", "Apr", "May", "Jun",
    "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
]

// === Determine week numbers and fiscal info ===
int currentWeekNum = calendar.get(Calendar.WEEK_OF_YEAR)  // Current week number
int priorWeekNum = currentWeekNum - 1                     // Previous week number
int currentWeekYear = calendar.get(Calendar.YEAR)        // Calendar year
String currentWeekFY = "FY" + (currentWeekYear % 100)   // Fiscal year shorthand, e.g., FY25
String currentWeekLabel = "Week ${currentWeekNum}"       // Label for current week
String priorWeekLabel = "Week ${priorWeekNum}"           // Label for prior week

// === Output storage ===
// Builders to accumulate string outputs for current and prior weeks
StringBuilder currentWeekOutputBuilder = new StringBuilder()
StringBuilder priorWeekOutputBuilder = new StringBuilder()

// === Helper to collect 7 days starting from a given Monday ===
// Closure returns a list of [Day, Month, FY] tuples for a 7-day week
Closure<List<List<Serializable>>> collectWeek = { Calendar startCal ->
    List<List<Serializable>> result = []
    Calendar temp = (Calendar) startCal.clone()
    for (int i = 0; i < 7; i++) {
        int yr = temp.get(Calendar.YEAR)
        int mo = temp.get(Calendar.MONTH)
        int dom = temp.get(Calendar.DAY_OF_MONTH)
        String day = "Day ${String.format('%02d', dom)}"      // Format day with leading zero
        String period = monthNames.get(mo).toUpperCase()      // Month abbreviation
        String fy = "FY" + (yr % 100)                         // Fiscal year shorthand

        result.add([day, period, fy] as List<Serializable>)  // Add tuple to week list
        temp.add(Calendar.DAY_OF_MONTH, 1)                   // Move to next day
    }
    return result
}

// === Align calendar to start of the week ===
// Ensures calculation always starts on Monday
Calendar alignedCal = (Calendar) calendar.clone()
int dow = alignedCal.get(Calendar.DAY_OF_WEEK)
int daysToBackUp = (dow == Calendar.MONDAY) ? 0 : ((dow + 5) % 7)
alignedCal.add(Calendar.DAY_OF_MONTH, -daysToBackUp)

// === Get prior week calendar ===
Calendar priorWeekCal = (Calendar) alignedCal.clone()
priorWeekCal.add(Calendar.DAY_OF_MONTH, -7)  // Move back 7 days to get prior week

// === Collect days for prior and current weeks ===
List<List<Serializable>> priorWeekDays = collectWeek(priorWeekCal)
List<List<Serializable>> currentWeekDays = collectWeek(alignedCal)

// === Build string outputs for prior week ===
priorWeekOutputBuilder.append("${priorWeekLabel} = \n")
for (int i = 0; i < priorWeekDays.size(); i++) {
    List<Serializable> line = priorWeekDays.get(i)
    String suffix = (i < priorWeekDays.size() - 1) ? "+" : ""  // Add "+" between days except last
    priorWeekOutputBuilder.append("[${line[0]}],[${line[1]}],[${line[2]}]${suffix}\n")
}

// === Build string outputs for current week ===
currentWeekOutputBuilder.append("${currentWeekLabel} = \n")
for (int i = 0; i < currentWeekDays.size(); i++) {
    List<Serializable> line = currentWeekDays.get(i)
    String suffix = (i < currentWeekDays.size() - 1) ? "+" : ""  // Add "+" between days except last
    currentWeekOutputBuilder.append("[${line[0]}],[${line[1]}],[${line[2]}]${suffix}\n")
}

// === Final string results ===
String currentWeekOutput = currentWeekOutputBuilder.toString()
String priorWeekOutput = priorWeekOutputBuilder.toString()

// === Output values of interest for diagnostic purposes ===
println "=== Diagnostic Output ==="
println "Trial Date: $trialTime"
println "Current Week Number: $currentWeekNum"
println "Current Week Label: $currentWeekLabel"
println "Current Week Fiscal Year: $currentWeekFY"
println "Prior Week Number: $priorWeekNum"
println "Prior Week Label: $priorWeekLabel"
println "\n=== Current Week Output ===\n$currentWeekOutput"
println "\n=== Prior Week Output ===\n$priorWeekOutput"
      </script>
    </rule>
  </rules>
  <components/>
</HBRRepo>
