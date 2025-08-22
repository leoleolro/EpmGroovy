/* RTPS:{Var_Source_File_Name} */

/* Get the cube where the calculations will take place*/
Cube cube = operation.application.getCube("ProjRep") //set cube
Set&lt;List&lt;String>> strProjectStatusArray = []
String strProjectStatus
String strProject
String strEntity


String strFileName = rtps.Var_Source_File_Name
// Below code will get the name of the Project 
strProject = "PRJ_" + strFileName.split("_")[0]

/* Creating the 1.01 Project properties form using flexibleDataGridDefinitionBuilder in the memory */
def dataGrid = cube.flexibleDataGridDefinitionBuilder() //set grid def obj
dataGrid.setPov(strProject,"No Scenario","No Version","No Year","Load","No View","BegBalance","No Line Item") //set POV members
dataGrid.addColumn("Project Status") //set column members
dataGrid.addRow(['ILvl0Descendants("All Entities")'])

// Load a data grid from the specified grid definition and cube
cube.loadGrid(dataGrid.build(), false).withCloseable { grid ->
  grid.dataCellIterator().each { DataCell cell ->
     if(cell.formattedValue.trim() != '' ) {
     	strProjectStatusArray &lt;&lt; ([cell.getEntityName(), cell.crossDimCell("Project Status").formattedValue]).toUnique()
        /*strProjectStatusArray &lt;&lt; ([cell.crossDimCell("Project Status").formattedValue]).toUnique()*/
		}
	}
}

if(strProjectStatusArray.size()==0){
 println("No Entity in the form created above in the memory.")
}
else{
  /* Loop through each tuples to get line item and Rate combination. */
 for (tuple in strProjectStatusArray) {
 	strEntity = tuple[0]
    strProjectStatus = tuple[1]
    
    if (strProjectStatus == "Active")
		{
    	operation.getApplication().getCube('ProjRep').clearPartialData("CrossJoin(CrossJoin(CrossJoin(CrossJoin(CrossJoin(CrossJoin(CrossJoin(CrossJoin({[Plan]},{[Working]}),{[No Year]}),{[Load]}),{[No View]}),{[${strEntity}]}),{[${strProject}]}),{[BegBalance]}),{Descendants([Pay Item Assumptions], [Pay Item Assumptions].dimension.Levels(0))})",true)
    	}
	else
		{
		println("Project Status for " + strProject + " is not set to Active." )
		}
}   
}</script></rule></rules><components/><deployobjects><deployobject product="2" application="rpepm" plantype="projrep" obj_id="1" obj_type="1" name="PROJREP - CLEAR LINEITEMS_MASTERDATA"/></deployobjects></HBRRepo>