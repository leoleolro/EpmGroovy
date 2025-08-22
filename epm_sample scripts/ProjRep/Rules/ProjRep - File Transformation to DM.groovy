Purpose : This rule is to trigger the pipeline and read te file name - project number, serach for project categoiry and project entity and write back to file.

String strFileName = rtps.Var_Source_File_Name.toString()
String strDMINBOXPath = "inbox/Data_Files/"

String strSourceFilePath = strDMINBOXPath  + strFileName
/*String transformedFileName = strFileName.split("\\.")[0] + "_T.csv"*/
String transformedFileName = "LineItem_MasterData.csv"

Set&lt;List&lt;String>> strProjectArray = []
String strProjectCategory
String strProjectEntity

// Below code will get the name of the Project
String strProjectName = "PRJ_" + strFileName.split("_")[0]

/* Get the cube where the calculations will take place*/
Cube cube = operation.application.getCube("ProjRep") //set cube

/* Creating the 1.01 Project properties form using flexibleDataGridDefinitionBuilder in the memory */
def dataGrid = cube.flexibleDataGridDefinitionBuilder() //set grid def obj
dataGrid.setPov("No Scenario","No Version","No Year","Load","No View","BegBalance","No Line Item") //set POV members
dataGrid.setPov("No Scenario","Working","No Year","Load","No View","BegBalance","No Line Item") //set POV members
dataGrid.addColumn("Project Type") //set column members
dataGrid.addRow('ILvl0Descendants("All Entities")',strProjectName)
dataGrid.setSuppressMissingBlocks(true)
dataGrid.setSuppressMissingSuppressesZero(true)
dataGrid.setSuppressMissingRows(true)

// Load a data grid from the specified grid definition and cube
cube.loadGrid(dataGrid.build(), false).withCloseable { grid ->
  // It will only iterate for the respective Project for which the file is coming as we have passed "strProjectName" in the below line.
  grid.dataCellIterator(strProjectName).each { DataCell cell ->
     if(cell.formattedValue.trim() != '' ) {
     	strProjectArray &lt;&lt; ([cell.getMemberName("Project"),cell.getMemberName("Entity"),cell.crossDimCell("Project Type").formattedValue]).toUnique()
		}
	}
}

strProjectCategory = strProjectArray[0][2]
strProjectEntity = strProjectArray[0][1]

/*
Sleep is needed for Project data file to get copied first and then the transformed file to create. If we don't put sleep
then sometime 100311_501001_T.csv gets created before 100311_501001.csv which then gives error as there is not source file to transform from.
*/
sleep(2000)

// We need only entity from "41105 - Redpath Australia Pty Limited", hence split and trim is used.
String strEntity = strProjectEntity

// Open CSV Writer for the transformed file
csvWriter(transformedFileName).withCloseable { outputFile ->

    // Write header row to the output file
    outputFile.writeNext(["Entity", "Project_Number", "Project_Type", "LineItem", "Account", "Data"] as String[])

    // Read the input CSV file
    csvIterator(strFileName).withCloseable { inputFile ->

        boolean isHeader = true  // Flag to handle header
        List&lt;String> headers = []

        // Process each row
        inputFile.each { row ->

            // Capture header row and continue to next iteration
            if (isHeader) {
                headers = row.toList()
                isHeader = false
                return
            }

            // Extract fixed columns
            String projectNumber = strProjectName
            String projectCategory = strProjectCategory
            String lineItem = row[headers.indexOf("line_number")]

            // Define the columns that should be transformed into rows
            List&lt;String> accountColumns = ["description","unit", "quantity", "estimated_rate", "estimated_total",
                                           "overhead","sell_rate", "sell_total","contract_number",
                                           "line_start_date", "line_end_date"]

            // Loop through each account column and create rows
            accountColumns.each { colName ->
                if (headers.contains(colName)) {
                    String value = row[headers.indexOf(colName)] ?: ""  // Handle blank values
                    outputFile.writeNext([strEntity, projectNumber, projectCategory, lineItem, colName, value] as String[])
                }
            }
        }
    }
}


/*HttpResponse&lt;String> jsonResponse1 = operation.application.getConnection("Pipeline").post()
.body(json(["jobType":"pipeline", "jobName":"ss2", "variables":
["SourceFileName":transformedFileName, "TargetFileName":strDMINBOXPath + transformedFileName]])).asString();
*/</script></rule></rules><components/><deployobjects><deployobject product="2" application="rpepm" plantype="projrep" obj_id="1" obj_type="1" name="PROJREP - FILE TRANSFORMATION TO DM"/></deployobjects></HBRRepo>