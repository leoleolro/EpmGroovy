
String strProjectName
String strStartDate
String strEndDate
//String strStartMonth
//String strStartYear
//String strEndMonth
//String strEndYear
def strBegBalanceData = []
def strAllYearData = []
def strAllYearDataUnique

Set&lt;List&lt;String>> strProjectArray = []
/* Get the cube where the calculations will take place*/
Cube cube1 = operation.application.getCube("ProjRep") //set cube

operation.grid.dataCellIterator("BegBalance","No Year").each {
	 strBegBalanceData &lt;&lt; ([it.getMemberName("Account"), (it.data).toString()]).toUnique()
}

println(strBegBalanceData)

String strYear = ["All Years"]
String strPeriod = ["YearTotal"]

operation.grid.dataCellIterator({DataCell cell -> strYear.contains(cell.getMemberName("Year")) &amp;&amp; strPeriod.contains(cell.getMemberName("Period"))}).each { DataCell cell ->
	strAllYearData &lt;&lt; ([cell.getMemberName("Account"), (cell.data).toString()]).toUnique()
}

strAllYearDataUnique = strAllYearData.unique()
println(strAllYearData)

def isDataMismatch = 0
def tolerance = 1000  // Set the mismatch tolerance

for (int i = 0; i &lt; strBegBalanceData.size(); i++) {

	def item1 = strBegBalanceData[i] as List  // force cast to List
    def item2 = strAllYearData[i] as List

    def blabel = item1[0]
    def ylabel = item2[0]
    def value1 = item1[1] != null ? new BigDecimal(item1[1].toString().trim()) : 0
    def value2 = item2[1] != null ? new BigDecimal(item2[1].toString().trim()) : 0
    println("blabel: ${blabel}, value1: ${value1}, ylabel: ${ylabel}, value2: ${value2}" )

    if ((value1 - value2).abs() > tolerance) {
        println(" Mismatch   --> blabel: ${blabel}, value1: ${value1}, ylabel: ${ylabel}, value2: ${value2}" )
        isDataMismatch = 1
    }

}

if (isDataMismatch == 0){
    operation.grid.dataCellIterator().each {
    strProjectName = it.getMemberName("Project")
    return
	}

    // Creating the 1.01 Project properties form using flexibleDataGridDefinitionBuilder in the memory
    FlexibleDataGridDefinitionBuilder dataGrid1 = cube1.flexibleDataGridDefinitionBuilder() //set grid def obj
    dataGrid1.setPov('{Var_Dim_Entity}',"No Scenario","Working","No Year","Load","No View","BegBalance","No Line Item") //set POV members
    dataGrid1.addColumn("Project Start Date", "Project End Date") //set column members
    dataGrid1.addRow('ILvl0Descendants(All Projects)')  //set row members

    // Load a data grid from the specified grid definition and cube
    cube1.loadGrid(dataGrid1.build(), false).withCloseable { grid ->
      // Restricting the iterator to check only for the Project which is selected in 1.04 Form.
      grid.dataCellIterator(strProjectName).each { DataCell cell ->
         if(cell.formattedValue.trim() != '' ) {
            strProjectArray &lt;&lt; ([cell.getMemberName("Project"),cell.crossDimCell("Project Start Date").formattedValue,cell.crossDimCell("Project End Date").formattedValue]).toUnique()

            }
        }
         
        if(strProjectArray.size() == 0) {
          println("No Data in 1.01 Project Properties Form for the project " + strProjectName )
        }
        else {
          strStartDate = strProjectArray[0][1]
          strEndDate = strProjectArray[0][2]         
         }
    }
/*
    Date date = new Date()
    Date date1 = new Date()
    
    
    date = new Date().parse('dd-MM-yyyy', '$strStartDate')
    strStartMonth = date.format('MMM')
    strStartYear = "FY" + date.format('yyyy').drop(2)

    date1 = new Date().parse('dd-MM-yyyy', '$strEndDate')
    strEndMonth = date1.format('MMM')
    strEndYear = "FY" + date1.format('yyyy').drop(2)*/

    /*//Set the Substitution Variable
    cube1.setSubstitutionVariableValue("Start_Year", strStartYear)
    cube1.setSubstitutionVariableValue("Start_Month", strStartMonth)
    cube1.setSubstitutionVariableValue("End_Year", strEndYear)
    cube1.setSubstitutionVariableValue("Ending_Month", strEndMonth)*/


    // This will create a date object with the current time
    
    SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy")
    Date date2 = new Date()
    String todays_date = sdf.format(date2)
    def strFilename = "PB_" + '{Var_Dim_Project}'.replace('"', '').replace("PRJ_", "") + "_" + todays_date + ".csv"
 
    Cube cube = operation.application.getCube("ProjRep") //set cube
    FlexibleDataGridDefinitionBuilder dataGrid = cube.flexibleDataGridDefinitionBuilder() //set grid def obj
    dataGrid.setPov('{Var_Dim_Entity}',"Plan","Working",'{Var_Dim_Project}') //set POV members

    dataGrid.addColumn("Descendants(All Years)","Lvl0Descendants(YearTotal)","Direct Input","Month","COM_7")//set column members
    dataGrid.addRow('ILvl0Descendants(Project Cost Code Budget)')  //set row members
    //set suppression settings
    dataGrid.setSuppressMissingRows(true)
    dataGrid.setSuppressMissingColumns(true)
    dataGrid.setSuppressMissingBlocks(true)
    dataGrid.setSuppressMissingSuppressesZero(true)
   
    //load grid to memory
    DataGrid gridToExport = cube.loadGrid(dataGrid.build(),false)
    operation.exportDataToFile(gridToExport,strFilename) //export grid
    
    sleep(3000)
    
    // Store all transformed rows
    def transformedData = []

    boolean isFirstRow = true
	
    // Step 1: Read the file and apply transformations
    csvIterator(strFilename).withCloseable { inputFileRows ->
        inputFileRows.each { inputFileRow ->

            List&lt;String> transformedRow = inputFileRow as List&lt;String>
            if (isFirstRow) {
                // Keep header as-is
                isFirstRow = false
            } else {
            
            	// Uncomment below line if the requirement is to remove the ENT_ from Entity name.
            	//transformedRow[0] = transformedRow[0].replace("ENT_", "").trim()
                
                 // Get the last column value (assumes it's numeric or blank)
            	def lastValue = transformedRow[-1]?.trim()

            	// Skip row if last value is blank or zero
            	if (!lastValue || lastValue == "0") {
                	//println("Skipping row due to blank or zero in last column: ${transformedRow}")
                	return
            	}
                
                // Remove "PRJ_" from Project column (index 3)
                transformedRow[3] = transformedRow[3].replace("PRJ_", "")
                
                // Column 5 (index 4): Remove "Budget" if present
            	transformedRow[4] = transformedRow[4].replace("Budget", "").trim()

                // Extract number after underscore from Line Item column (index 9)
                def lineItem = transformedRow[9]
                if (lineItem.contains("_")) {
                    transformedRow[9] = lineItem.split("_")[1]
                    
                }
                               
            }

            // Add transformed row to list
            transformedData &lt;&lt; transformedRow
        }
    }

    // Step 2: Overwrite the original file with transformed content
    csvWriter(strFilename).withCloseable { writer ->
        transformedData.each { row ->
            writer.writeNext(row as String[])
        }
    }
	
    sleep(2000)   

    String strDMINBOXPath = "inbox/Project_Phased_Budget/"
    HttpResponse&lt;String> jsonResponse1 = operation.application.getConnection("Pipeline").post()
    .body(json(["jobType":"pipeline", "jobName":"DE2", "variables":
    ["SourceFileName":strFilename, "TargetFileName":strDMINBOXPath + strFilename]])).asString();
    println("Data Exported Successfully.")
}
</script></rule></rules><components/><deployobjects><deployobject product="2" application="rpepm" plantype="projrep" obj_id="1" obj_type="1" name="PROJREP - COST CODE BUDGET EXPORT DATA"/></deployobjects></HBRRepo>