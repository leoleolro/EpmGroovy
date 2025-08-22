
<HBRRepo>
  <variables/>
  <rulesets/>
  <rules>
    <rule id="1" name="Physicals - Week Dates" product="Planning">
      <property name="application">RPEPM</property>
      <property name="plantype">Physical</property>
      <script type="groovy">
/* RTPS: */
import java.text.SimpleDateFormat

Calendar calendar = new GregorianCalendar()
Date trialTime = new Date()  // Context: Get today's date
calendar.setTime(trialTime)
calendar.setFirstDayOfWeek(Calendar.MONDAY) // Context: Define Monday as first day of the week
calendar.setMinimalDaysInFirstWeek(7)       // Context: Ensures ISO-style week handling

int currentWeekYear = calendar.get(Calendar.YEAR)  
String currentWeekFY = "FY" + (currentWeekYear % 100)  
// Use Case: Fiscal Year label for cube POVs (e.g., FY25)

int currentWeekNum = calendar.get(Calendar.WEEK_OF_YEAR)  
String currentWeekLabel = "Week ${currentWeekNum}"  
// Context: Create readable "Week N" label for substitution variable


// === Closure to calculate a weekly date range ===
Closure&lt;List&lt;String>> collectWeek = { Calendar startCal ->
    Calendar temp = (Calendar) startCal.clone()
    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd")

    String firstDate = sdf.format(temp.getTime())  // Context: Monday date
    temp.add(Calendar.DAY_OF_MONTH, 6)             // Context: Move forward to Sunday
    String lastDate = sdf.format(temp.getTime())   // Context: Sunday date

    return [firstDate, lastDate]  // Use Case: Provides week start & end for assignment
}

// === Align calendar to start of the week ===
Calendar alignedCal = (Calendar) calendar.clone()
int dow = alignedCal.get(Calendar.DAY_OF_WEEK)
int daysToBackUp = (dow == Calendar.MONDAY) ? 0 : ((dow + 5) % 7)
// Context: Calculate how many days to roll back to reach Monday
alignedCal.add(Calendar.DAY_OF_MONTH, -daysToBackUp)

List&lt;String> currentWeekRange = collectWeek(alignedCal)
String currentWeekStartDate = currentWeekRange[0]
String currentWeekEndDate = currentWeekRange[1]

println("Week: " + currentWeekFY )
println(currentWeekLabel)
println(currentWeekStartDate + " " + currentWeekEndDate)

// Use Case: Substitute variable to track the "current week"
operation.application.setSubstitutionVariableValue("CurrentWeek", currentWeekLabel)

Cube cube = operation.getApplication().getCube('Physical')
CustomCalcParameters calcParameters = new CustomCalcParameters()

// Year user variable from Planning webform/user input
def strYear = operation.application.getUserVariable("UV_Year").value.name

// POV (point of view) definition for ASO calculation
def povData = [
    ['[No Scenario]'],
    ['[No Version]'],
    ['[Load]'],
    ['[No View]'],
    ['[No Asset]'],
    ['[BegBalance]'],
    ['[No Project]'],
    ['[No Entity]']
]

calcParameters.pov = getCrossJoins(povData)


// === Assign Week Start Date into cube ===
String script = """
([Week Start Date],[${currentWeekLabel}],[${currentWeekFY}]) := ${currentWeekStartDate};
"""
// Context: Dynamically writes start date to the ASO cube at week/fiscal year intersection
calcParameters.script = script
calcParameters.roundDigits = 2
cube.executeAsoCustomCalculation(calcParameters)


// === Assign Week End Date into cube ===
script = """
([Week End Date],[${currentWeekLabel}],[${currentWeekFY}]) := ${currentWeekEndDate};
"""
// Context: Writes end date to cube
calcParameters.script = script
calcParameters.roundDigits = 2
cube.executeAsoCustomCalculation(calcParameters)


// === Helper function to create Essbase CrossJoin POVs ===
def getCrossJoins(List&lt;List&lt;String>> essIntersection) {
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
    <deployobject product="2" application="rpepm" plantype="physical" obj_id="1" obj_type="1" name="PHYSICALS - WEEK DATES"/>
  </deployobjects>
</HBRRepo>
