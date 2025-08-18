/*Rule Name : ProjRep - Update File With Project Classification
Purpose : This rule is to trigger the pipeline and read te file name - project number, search for project category and write back to file.

import org.json.JSONObject
import groovy.json.JsonSlurper


String strFileName = rtps.Var_Source_File_Name.toString()
/*String strFileName = "100311_501001.csv"*/
String strDMINBOXPath = "inbox/"

String strSourceFilePath = strDMINBOXPath  + strFileName
String transformedFileName = strFileName.split("\\.")[0] + "_T.csv"

Set&lt;List&lt;String>> strProjectArray = []
String strProjectCategory
String strProjectEntity

//print the variables
println("FileName is : $strFileName")
println("Complete Target FileName is : $strSourceFilePath")
println("TransformedFileName is : $transformedFileName")


// Below code will get the name of the Project 
String strProjectName = "PRJ_" + strFileName.split("_")[0]
println("Project Name: " + strProjectName)


/* Get the cube where the calculations will take place*/
Cube cube = operation.application.getCube("ProjRep") //set cube

/* Creating the 1.01 Project properties form using flexibleDataGridDefinitionBuilder in the memory */
def dataGrid = cube.flexibleDataGridDefinitionBuilder() //set grid def obj
/*dataGrid.setPov("No Scenario","No Version","No Year","Load","No View","BegBalance","No Line Item") //set POV members*/
dataGrid.setPov("No Scenario","Working","No Year","Load","No View","BegBalance","No Line Item") //set POV members
dataGrid.addColumn("Project Type") //set column members
dataGrid.addRow('ILvl0Descendants("All Entities")',strProjectName)
dataGrid.setSuppressMissingBlocks(true)
dataGrid.setSuppressMissingSuppressesZero(true)
dataGrid.setSuppressMissingRows(true)



// Load a data grid from the specified grid definition and cube
cube.loadGrid(dataGrid.build(), false).withCloseable { grid ->
  grid.dataCellIterator.each { DataCell cell ->
     if(cell.formattedValue.trim() != '' ) {
     	strProjectArray &lt;&lt; ([cell.getMemberName("Project"),cell.getMemberName("Entity"),cell.crossDimCell("Project Type").formattedValue]).toUnique()
		}
	}
}

println ("strProjectArray : " +  strProjectArray[0][1] + " and " + "strProjectArray : " + strProjectArray[0][2])
strProjectCategory = strProjectArray[0][2]
strProjectEntity = strProjectArray[0][1]

/*
Sleep is needed for Project data file to get copied first and then the transformed file to create. If we don't put sleep
then sometime 100311_501001_T.csv gets created before 100311_501001.csv which then gives error as there is not source file to transform from.
*/
sleep(2000)

// Create a CSV Writer with Transformed File Name
csvWriter(transformedFileName).withCloseable { outputFileRows ->	

    boolean isFirstRow = true  // Track if it's the first row

	// Iterate through uploaded file which is copied to Inbox/Outbox explorer
	csvIterator(strFileName).withCloseable { inputFileRows ->    	
		inputFileRows.each { inputFileRow ->
            
            List&lt;String> transformedRow = inputFileRow as List&lt;String>

            // Append "Project Classification" to the first row as a header
            if (isFirstRow) {
                transformedRow.add("Project Classification")
                transformedRow.add("Project Entity")
                isFirstRow = false  // Mark first row as processed
            } else {
                // Append strProjectCategory in subsequent rows
                transformedRow.add(strProjectCategory)
                transformedRow.add(strProjectEntity)
            }

            // Write the transformed row to output file
			outputFileRows.writeNext(transformedRow as String[])  

            // Print in a readable format
            println transformedRow.join(", ")
    	}
	}    
}


</script></rule></rules><components/><deployobjects><deployobject product="2" application="rpepm" plantype="projrep" obj_id="1" obj_type="1" name="PROJREP - UPDATE FILE WITH PROJECT CLASSIFICATION"/></deployobjects></HBRRepo>