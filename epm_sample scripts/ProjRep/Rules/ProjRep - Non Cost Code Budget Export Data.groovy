Rule Name : ProjRep - Cost Code Budget Export Data
/* RTPS: {Var_Dim_Entity} {Var_Dim_Project}*/


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


operation.grid.dataCellIterator().each {
    strProjectName = it.getMemberName("Project")
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

/*def date = new Date()
def date1 = new Date()

date = new Date().parse('dd-MM-yyyy', "$strStartDate")
strStartMonth = date.format('MMM')
strStartYear = "FY" + date.format('yyyy').drop(2)

date1 = new Date().parse('dd-MM-yyyy', "$strEndDate")
strEndMonth = date1.format('MMM')
strEndYear = "FY" + date1.format('yyyy').drop(2)
*/

// This will create a date object with the current time
/*def date2 = new Date()
def todays_date = date2.format('ddMMyyyy')
def strFilename = "PB_" + '{Var_Dim_Project}'.replace('"', '').replace("PRJ_", "") + "_" + todays_date + ".csv"*/

SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy")
Date date2 = new Date()
String todays_date = sdf.format("$date2")
String strFilename = "PB_" + '{Var_Dim_Project}'.replace('"', '').replace("PRJ_", "") + "_" + todays_date + ".csv"

Cube cube = operation.application.getCube("ProjRep") //set cube
FlexibleDataGridDefinitionBuilder dataGrid = cube.flexibleDataGridDefinitionBuilder() //set grid def obj
//dataGrid.setPov('{Var_Dim_Entity}',"Plan","Working",'{Var_Dim_Project}') 
dataGrid.setPov('{Var_Dim_Entity}',"Plan","Working",'{Var_Dim_Project}',"Direct Input") //set POV members

dataGrid.addColumn("Descendants(All Years)","Lvl0Descendants(YearTotal)","Month")//set column members
dataGrid.addRow('Lvl0Descendants(Project Cost Code Budget)','Lvl0Descendants(Common Tasks)')  //set row members
dataGrid.addRow('Lvl0Descendants(Project Cost Code Budget)','Lvl0Descendants(Operational Tasks)') 


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
        /*transformedRow[0] = transformedRow[0].replace("ENT_", "").trim()*/

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

println("strFilename: " + strFilename)

String strDMINBOXPath = "inbox/Project_Phased_Budget/"
HttpResponse&lt;String> jsonResponse1 = operation.application.getConnection("Pipeline").post()
.body(json(["jobType":"pipeline", "jobName":"DE2", "variables":
            ["SourceFileName":strFilename, "TargetFileName":strDMINBOXPath + strFilename]])).asString();
println("Data Exported Successfully.")

println(jsonResponse1.body)

</script></rule></rules><components/><deployobjects><deployobject product="2" application="rpepm" plantype="projrep" obj_id="1" obj_type="1" name="PROJREP - NON COST CODE BUDGET EXPORT DATA"/></deployobjects></HBRRepo>