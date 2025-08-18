Purpose : This rule is to lock the period and year based on project start date and project end date and create blocks if there no data in the periods.

/* RTPS: {Var_Dim_Project} ,{Var_Dim_Entity} */

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
String strProjectName
String strEntityName
String strStartDate
String strEndDate
String strEndMonth
String strEndYear
def strMonthsToBlock
String strBlockTillMonth
String strBlockTillYear
String strBlockTillYearinFY
def strBegBalanceNoYearData = 0
def strPeriodYearData = 0


/* Get the cube where the calculations will take place*/
Cube cube = operation.application.getCube("ProjRep") //set cube

Set&lt;List&lt;String>> strProjectArray = []
//def date = new Date()
//def date1 = new Date()

strProjectName = stripQuotes(rtps.Var_Dim_Project.toString())
strEntityName = stripQuotes(rtps.Var_Dim_Entity.toString())
String strProjectNameNoPrefix = strProjectName.tokenize('_')[1]
String strProjectTasks = "${strProjectNameNoPrefix}_Tasks"
println(strProjectName)

// Creating the "1.01 Project properties form" using flexibleDataGridDefinitionBuilder in the memory to fetch the start and end date of the Project selected on the current form.
FlexibleDataGridDefinitionBuilder dataGrid = cube.flexibleDataGridDefinitionBuilder() //set grid def obj
dataGrid.setPov(strEntityName,"No Scenario","Working","No Year","Load","No View","BegBalance","No Line Item") //set POV members
dataGrid.addColumn("Project Start Date", "Project End Date") //set column members
dataGrid.addRow("ILvl0Descendants(All Projects)")  //set row members

// Load a data grid from the specified grid definition and cube
cube.loadGrid(dataGrid.build(), false).withCloseable { grid ->
  grid.dataCellIterator.each { DataCell cell ->
     if(cell.formattedValue.trim() != '' ) {
     	strProjectArray &lt;&lt; ([cell.getMemberName("Project"),cell.crossDimCell("Project Start Date").formattedValue,cell.crossDimCell("Project End Date").formattedValue]).toUnique()
		//println(strProjectArray)
		}
	}

    // Write an entry into the log file if no cells have been edited and exit the script.
    if(strProjectArray.size() == 0) {
    }
    else {
      // Loop through each tuples to get the start and end date.
      for (tuple in strProjectArray) {
        if (tuple[0] == strProjectName)
        {
          strStartDate = tuple[1]
          strEndDate = tuple[2]
        }
      }
		// Based on the EndDate, gets the End Month and End Year it will make all the cells readonly after this date.
  /*	  date1 = new Date().parse('dd-MM-yyyy', "$strEndDate")
      strEndMonth = date1.format('MMM')
      strEndYear = "FY" + date1.format('yyyy').drop(2)*/
      
          SimpleDateFormat sdf1 = new SimpleDateFormat("dd-MM-yyyy")
	Date date1 = sdf1.parse("$strEndDate")
    SimpleDateFormat Month1 = new SimpleDateFormat("MMM")
   strEndMonth = Month1.parse("$strEndDate")
      //strBlockTillMonth = date.format('MMM')
      SimpleDateFormat SYear1 = new SimpleDateFormat("YY")
      strEndYear = "FY" + SYear1.parse("$strEndDate")

      if (strEndMonth == "Jan" ){
      	strMonthsToBlock = ["Feb","Mar","Apr", "May", "Jun","Jul","Aug","Sep","Oct","Nov","Dec"]}
      else if (strEndMonth == "Feb" ){
      	strMonthsToBlock = ["Mar","Apr", "May", "Jun","Jul","Aug","Sep","Oct","Nov","Dec"]}
      else if (strEndMonth == "Mar" ){
      	strMonthsToBlock = ["Apr", "May", "Jun","Jul","Aug","Sep","Oct","Nov","Dec"]}
      else if (strEndMonth == "Apr" ){
      	strMonthsToBlock = ["May", "Jun","Jul","Aug","Sep","Oct","Nov","Dec"]}
      else if (strEndMonth == "May" ){
      	strMonthsToBlock = ["Jun","Jul","Aug","Sep","Oct","Nov","Dec"]}
      else if (strEndMonth == "Jun" ){
      	strMonthsToBlock = ["Jul","Aug","Sep","Oct","Nov","Dec"]}
      else if (strEndMonth == "Jul" ){
      	strMonthsToBlock = ["Aug","Sep","Oct","Nov","Dec"]}
      else if (strEndMonth == "Aug" ){
      	strMonthsToBlock = ["Sep","Oct","Nov","Dec"]}
      else if (strEndMonth == "Sep" ){
      	strMonthsToBlock = ["Oct","Nov","Dec"]}
      else if (strEndMonth == "Oct" ){
      	strMonthsToBlock = ["Nov","Dec"]}
      else if (strEndMonth == "Nov" ){
      	strMonthsToBlock = ["Dec"]}

       // Extract the year from EndDate
      def dateParts = strEndDate.split("-")
      def year = dateParts[2] as Integer 

      // Determine the starting fiscal year (next year from EndDate)
      def nextYear = year + 1  // FY starts from the next year

	  // Fetch the Last and First year of the application dynamically.
	  Dimension yearDim = operation.application.getDimension("Year", cube)
      def allYears = yearDim.getEvaluatedMembers("ILvl0Descendants(All Years)", cube)

      //Get the last year of the application
      String lastYear = allYears.last()
      def endYear = ("20" + lastYear.replace("FY", "")).toInteger()

	  // Generate the year list for the future Years to be blocked.
      def fiscalYears = (nextYear..endYear).collect { "FY" + it.toString().substring(2) }

      // If the end month is Dec then blocking should happen from the nexy year Jan onwards. For .e.g If endDate is 31-12-2026 then blocking should happen from FY27.
	  if(strEndMonth == "Dec"){
      	operation.grid.dataCellIterator().each {
        if(fiscalYears.contains(it.getMemberName("Year")) ) {
          it.setForceReadOnly(true)
          }
          }

      }
      else {
      	operation.grid.dataCellIterator().each {
        if((strEndYear.contains(it.getMemberName("Year")) &amp;&amp; strMonthsToBlock.contains(it.getPeriodName())) || fiscalYears.contains(it.getMemberName("Year")) ) {
          it.setForceReadOnly(true)
          }
          }
      }

      // Below code gets the startMonth and start year from 1.01 Form and it will make all the cells readonly prior to this date.
  /*    date = new Date().parse('dd-MM-yyyy', "$strStartDate")
      strBlockTillMonth = date.format('MMM')
      strBlockTillYear = date.format('yyyy').toInteger()
      strBlockTillYearinFY = "FY" + date.format('yyyy').drop(2)*/
      
          SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy")
	Date date = sdf.parse("$strStartDate")
    SimpleDateFormat Month = new SimpleDateFormat("MMM")
   strBlockTillMonth = Month.parse("$strStartDate")
      //strBlockTillMonth = date.format('MMM')
      SimpleDateFormat SYear = new SimpleDateFormat("YY")
      strBlockTillYear = SYear.parse("$strStartDate")
      strBlockTillYearinFY = "FY" + "$strBlockTillYear"

      // Determine the starting fiscal year (next year from EndDate)
      def previousYear = strBlockTillYear - 1  // FY starts from the next year

      strMonthsToBlock = []

      if (strBlockTillMonth == "Feb" ){
      	strMonthsToBlock = ["Jan"]}
      else if (strBlockTillMonth == "Mar" ){
      	strMonthsToBlock = ["Jan","Feb"]}
      else if (strBlockTillMonth == "Apr" ){
      	strMonthsToBlock = ["Jan","Feb","Mar"]}
      else if (strBlockTillMonth == "May" ){
      	strMonthsToBlock = ["Jan","Feb","Mar","Apr"]}
      else if (strBlockTillMonth == "Jun" ){
      	strMonthsToBlock = ["Jan","Feb","Mar","Apr","May"]}
      else if (strBlockTillMonth == "Jul" ){
      	strMonthsToBlock = ["Jan","Feb","Mar","Apr","May","Jun"]}
      else if (strBlockTillMonth == "Aug" ){
      	strMonthsToBlock = ["Jan","Feb","Mar","Apr","May","Jun","Jul"]}
      else if (strBlockTillMonth == "Sep" ){
      	strMonthsToBlock = ["Jan","Feb","Mar","Apr","May","Jun","Jul","Aug"]}
      else if (strBlockTillMonth == "Oct" ){
      	strMonthsToBlock = ["Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep"]}
      else if (strBlockTillMonth == "Nov" ){
      	strMonthsToBlock = ["Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct"]}
      else if (strBlockTillMonth == "Dec" ){
      	strMonthsToBlock = ["Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Nov"]}

      //Get the first year of the application
      String firstYear = allYears.first()
      def startYear = ("20" + firstYear.replace("FY", "")).toInteger()

	  // Generate the year list for the future Years.
      def listYearsToBlock = (startYear..previousYear).collect { "FY" + it.toString().substring(2) }

       // If the month is Jan then blocking should happen for the entire previous year onwards. For .e.g If Date is 31-01-2025 then blocking should happen from FY24.
	  if(strEndMonth == "Jan"){
      	operation.grid.dataCellIterator().each {
      	if(listYearsToBlock.contains(it.getMemberName("Year")) ) {
          it.setForceReadOnly(true)
     		}
		}

      }
      else {
      	operation.grid.dataCellIterator().each {
      	if(((strBlockTillYearinFY.toString()).contains(it.getMemberName("Year")) &amp;&amp; strMonthsToBlock.contains(it.getPeriodName())) || listYearsToBlock.contains(it.getMemberName("Year")) ) {
          it.setForceReadOnly(true)
     		}
		}
      }	 	    
      
  }

}

DataGridBuilder builder = cube.dataGridBuilder("MM/DD/YYYY", SYSTEM_USER)

// Parse input dates
SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy")
Date startDate = sdf.parse(strStartDate)
Date endDate = sdf.parse(strEndDate)

Calendar cal = Calendar.getInstance()
cal.setTime(startDate)

// New lists to store year and period as parallel arrays
List&lt;String> fiscalYears = []
List&lt;String> fiscalMonths = []

while (!cal.getTime().after(endDate)) {
    int year = cal.get(Calendar.YEAR)
    int month = cal.get(Calendar.MONTH)

    // Determine fiscal year: assumes FY starts in Jan
    String fyKey = "FY" + String.format("%02d", year % 100)

    // Month name in uppercase (e.g., JAN, FEB, etc.)
    String monthName = cal.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.ENGLISH).toUpperCase()

    // Add to parallel lists
    fiscalYears &lt;&lt; fyKey
    fiscalMonths &lt;&lt; monthName

    // Move to next month
    cal.add(Calendar.MONTH, 1)
}


def fiscalYearsQuoted = fiscalYears.collect { "\"$it\"" }  // list of quoted strings
def fiscalMonthsQuoted = fiscalMonths.collect { "\"$it\"" }

println("Formatted fiscalYears: " + fiscalYearsQuoted)
println("Formatted fiscalMonths: " + fiscalMonthsQuoted)
println("fiscalYears.size(): " + fiscalYears.size())


builder.addPov(strEntityName,"Plan","Working",strProjectName,"Direct Input","Month") //set POV members

builder.invokeMethod("addColumn", fiscalYearsQuoted as Object[])
builder.invokeMethod("addColumn", fiscalMonthsQuoted as Object[])

// Expand all base members of both sets
def accountMembers = operation.application.getDimension('Account').getEvaluatedMembers("ILvl0Descendants(Project Cost Code Budget)", cube)
def commontaskMembers = operation.application.getDimension('Line Item').getEvaluatedMembers("ILvl0Descendants(Common Tasks)", cube)

// Cross join accounts and Common tasks, set 0s for each combination
accountMembers.each { acc ->
  commontaskMembers.each { task ->
    builder.addRow([acc.name, task.name], (1..fiscalYears.size()).collect { 0 })
  }
}


def operatoinaltaskMembers = operation.application.getDimension('Line Item').getEvaluatedMembers("ILvl0Descendants(${strProjectTasks})", cube)
// Cross join accounts and Operational tasks, set 0s for each combination
accountMembers.each { acc1 ->
  operatoinaltaskMembers.each { task1 ->
    builder.addRow([acc1.name, task1.name], (1..fiscalYears.size()).collect { 0 })
  }
}
     

DataGridBuilder.Status status = new DataGridBuilder.Status()
builder.build(status).withCloseable { grid ->
  println("Total number of cells accepted: $status.numAcceptedCells")
  println("Total number of cells rejected: $status.numRejectedCells")
  println("First 100 rejected cells: $status.cellsRejected")

  // Save the data to the cube
  cube.saveGrid(grid)
  
  }
</script></rule></rules><components/><deployobjects><deployobject product="2" application="rpepm" plantype="projrep" obj_id="1" obj_type="1" name="PROJREP - SEED NON COST TRACKING BUDGETS"/></deployobjects></HBRRepo>