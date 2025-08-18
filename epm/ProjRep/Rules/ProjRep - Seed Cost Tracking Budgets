Purpose : This rule is to lock the period and year based on project start date and project end date and create blocks if there no data in the periods.
/*RTPS: */

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
String strProjectName
String strEntityName
String strStartDate
String strEndDate
String strEndMonth
String strEndYear
List&lt;String> strMonthsToBlock
String strBlockTillMonth
def strBlockTillYear
String strBlockTillYearinFY
Double strBegBalanceNoYearData = 0
Double strPeriodYearData = 0


/* Get the cube where the calculations will take place*/
Cube cube = operation.application.getCube("ProjRep") //set cube

Set&lt;List&lt;String>> strProjectArray = []
Date date = new Date()
Date date1 = new Date()

// Below code will fetch the current Entity and Project from the Form.
operation.grid.dataCellIterator('Project Billed Revenue','No Year').each {
     strProjectName = it.getMemberName("Project")
     strEntityName = it.getEntityName()
}

// Creating the "1.01 Project properties form" using flexibleDataGridDefinitionBuilder in the memory to fetch the start and end date of the Project selected on the current form.
def dataGrid = cube.flexibleDataGridDefinitionBuilder() //set grid def obj
dataGrid.setPov(strEntityName,"No Scenario","Working","No Year","Load","No View","BegBalance","No Line Item") //set POV members
dataGrid.addColumn("Project Start Date", "Project End Date") //set column members
dataGrid.addRow('ILvl0Descendants(All Projects)')  //set row members

// Load a data grid from the specified grid definition and cube
cube.loadGrid(dataGrid.build(), false).withCloseable { grid ->
  grid.dataCellIterator.each { DataCell cell ->
     if(cell.formattedValue.trim() != '' ) {
     	strProjectArray &lt;&lt; ([cell.getMemberName("Project"),cell.crossDimCell("Project Start Date").formattedValue,cell.crossDimCell("Project End Date").formattedValue]).toUnique()

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
		
	 // Parse the input string to a Date object
    SimpleDateFormat sdf = new SimpleDateFormat('dd-MM-yyyy')
    date1 = sdf.parse(strEndDate)

    // Extract month and year
    strEndMonth = new SimpleDateFormat('MMM').format(date1)  // e.g., "Aug"
    String yearPart = new SimpleDateFormat('yyyy').format(date1)  // e.g., "2025"
    strEndYear = "FY" + yearPart.substring(2)  // e.g., "FY25"
    

	/* Below section of code is replaced by the above code due to new Groovy validation rules.
      // Based on the EndDate, gets the End Month and End Year it will make all the cells readonly after this date.
  	  date1 = new Date().parse('dd-MM-yyyy', "$strEndDate")
      strEndMonth = date1.format('MMM')
      strEndYear = "FY" + date1.format('yyyy').drop(2)
	*/
    


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

	// Convert string to Date object
    //SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy")
    date = sdf.parse(strStartDate)

    // Extract values
    strBlockTillMonth = new SimpleDateFormat("MMM").format(date)        // e.g., "Aug"
    strBlockTillYear = Integer.parseInt(new SimpleDateFormat("yyyy").format(date))  // e.g., 2025
    strBlockTillYearinFY = "FY" + new SimpleDateFormat("yy").format(date)        // e.g., "FY25"

    // Determine the previous fiscal year
    int previousYear = strBlockTillYear - 1
	

	/* Below section of code is replaced by the above code due to new Groovy validation rules.
      // Below code gets the startMonth and start year from 1.01 Form and it will make all the cells readonly prior to this date.
      date = new Date().parse('dd-MM-yyyy', "$strStartDate")
      strBlockTillMonth = date.format('MMM')
      strBlockTillYear = date.format('yyyy').toInteger()
      strBlockTillYearinFY = "FY" + date.format('yyyy').drop(2)

      // Determine the starting fiscal year (next year from EndDate)
      def previousYear = strBlockTillYear - 1  // FY starts from the next year
	*/


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

// Below will check if data at BegBalance is Time-phased or not, if not then it will put ZERO's so that block gets created and cells will be visible.
operation.grid.dataCellIterator('Project Billed Revenue').each {	
        if (it.getMemberNames().contains('BegBalance') &amp;&amp; it.getMemberNames().contains('No Year') ) 
        {
        	strBegBalanceNoYearData = it.data
        }
        else
        {	
        	strPeriodYearData = strPeriodYearData + it.data
        } 	
}

if (strBegBalanceNoYearData != 0.0 &amp;&amp; strPeriodYearData == 0.0)
{
	println("BegBalance has data but Jan-Dec period doesn't have any data so we need to insert ZERO in Jan-Dec to create blocks.")
	
	// Date format
	SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy")
	Date startDate = sdf.parse(strStartDate)
	Date endDate = sdf.parse(strEndDate)

	Calendar cal = Calendar.getInstance()
	cal.setTime(startDate)

	Map&lt;String, List&lt;String>> fyMap = new LinkedHashMap&lt;>()

	while (!cal.getTime().after(endDate)) {
		int year = cal.get(Calendar.YEAR)
		int month = cal.get(Calendar.MONTH) // 0 = Jan

		// Determine FY
		String fyKey = "FY" + String.format("%02d", year % 100)

		// Add month to FY bucket
		if (!fyMap.containsKey(fyKey)) {
			fyMap[fyKey] = []
		}

		String monthName = cal.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.ENGLISH).toUpperCase()
		if (!fyMap[fyKey].contains(monthName)) {
			fyMap[fyKey] &lt;&lt; monthName
		}

		// Next month
		cal.add(Calendar.MONTH, 1)
	}

	DataGridBuilder builder = cube.dataGridBuilder("MM/DD/YYYY")
	builder.addPov(strEntityName,"Plan","Working",strProjectName,"Direct Input","Month","COM_7") //set POV members

	// Print result
	fyMap.each { fy, months ->
		println("${fy}")
		builder.addColumn(fy)
		months.each { month ->
			println " - ${month}"
			builder.addColumn(month)//set column members
			builder.addRow(['Project Billed Revenue'],[0])
			DataGridBuilder.Status status = new DataGridBuilder.Status()
			DataGrid grid = builder.build(status)
			
			// Save the data to the cube
			cube.saveGrid(grid)			
		}
	}	
}
else if (strBegBalanceNoYearData == 0 &amp;&amp; strPeriodYearData == 0.0)
{
	println("There is no data for the Project itself.")
}
else
{
	println("Data is already Time-phased.")
}
</script></rule></rules><components/><deployobjects><deployobject product="2" application="rpepm" plantype="projrep" obj_id="1" obj_type="1" name="PROJREP - SEED COST TRACKING BUDGETS"/></deployobjects></HBRRepo>