Purpose : Use groovy to copy approved Budgets from BegBalance to the appropriate Months

 
/**************************************************************************************/
/* Use groovy to copy approved Budgets from BegBalance to the appropriate Months.               
/**************************************************************************************/
 
 Application app = operation.application
 Cube cube = app.getCube("ProjRep")
 
 
/**************************************************************************************/
/* Step 1: This section creates a grid with the loaded Budget from the ERP 
/**************************************************************************************/
 
DataGridDefinitionBuilder sourceBuilder = cube.dataGridDefinitionBuilder()
 
sourceBuilder.setSuppressMissingRowsNative(true)
sourceBuilder.setSuppressMissingBlocks(true)
 
sourceBuilder.addPov([ 'Version', 'Scenario', 'Period' ],
                    [  ['Final'], ['Plan'], ['BegBalance'] ] )
 
sourceBuilder.addColumn(['Plan Element'], [ ['Load'] ])
 
sourceBuilder.addRow(['Year' ,'Entity', 'Account', 'Project', 'Line Item', 'View'],
                       [
                       ['ILvl0Descendants("All Years")'],
                       ['ILvl0Descendants("All Entities")'],
                       ['ILvl0Descendants("Project Cost Codes")'],
                       ['ILvl0Descendants("All Projects")'],
                       ['ILvl0Descendants("Total Tasks")'],
                       ['LBUD_Jan','LBUD_Feb','LBUD_Mar','LBUD_Apr','LBUD_May','LBUD_Jun','LBUD_Jul','LBUD_Aug','LBUD_Sep','LBUD_Oct','LBUD_Nov','LBUD_Dec'] 
                        
                     ])
                     
                      
DataGridDefinition sourceGridDef = sourceBuilder.build()
def targetRows = []
 
/**************************************************************************************/
/* Step 2: This step applies the mapping of Budget Load member to Period            */
/**************************************************************************************/
cube.loadGrid(sourceGridDef, false).withCloseable { sourceGrid ->
 
  Map mappings = [
                /* Copy temp member to store member for auto predict */
                     'LBUD_Jan': 'Jan',
    				 'LBUD_Feb': 'Feb',
                     'LBUD_Mar': 'Mar',
                     'LBUD_Apr': 'Apr',
                     'LBUD_May': 'May',
                     'LBUD_Jun': 'Jun',
                     'LBUD_Jul': 'Jul',
                     'LBUD_Aug': 'Aug',
                     'LBUD_Sep': 'Sep',
                     'LBUD_Oct': 'Oct',
                     'LBUD_Nov': 'Nov',
                     'LBUD_Dec': 'Dec'
                ]
                 
 
  sourceGrid.rows.each { row ->
    List&lt;String> rowHeaders = row.headers.collect{mappings.containsKey(it.mbrName) ? mappings[it.mbrName] : it.mbrName}
    def rowData = []
    rowData = row.data.collect{it.missing ? '#Missing' : it.data}
    targetRows &lt;&lt; [rowHeaders, rowData]


}
 /*println "Extracted $targetRows"*/
}
 
/******************************************************************************************/
/* Step 3: This step creates the grid where the data will be copied to and copies the data                                 
/******************************************************************************************/
 
/* If the source grid (targetRows) contains data, continue, else go to the end of the rule without error */
if(targetRows){
 
DataGridBuilder targetGrid = cube.dataGridBuilder("DD/MM/YYYY") 
targetGrid.addPov( 'Final', 'Plan', 'Month')

targetGrid.addColumn ('Load')
 
targetRows.each {  row ->
 
   List listRow = (List) row
   targetGrid.addRow((List&lt; String>) listRow[0] , (List&lt;Double>) listRow[1])
 
}
 
DataGridBuilder.Status status = new DataGridBuilder.Status()
 
 
targetGrid.build(status).withCloseable { grid ->
  println("Total number of cells accepted: $status.numAcceptedCells")
  println("Total number of cells rejected: $status.numRejectedCells")
  println("First 100 rejected cells: $status.cellsRejected")
 
  // Save the data to the cube
 
  cube.saveGrid(grid)
}
}</script></rule></rules><components/><deployobjects><deployobject product="2" application="rpepm" plantype="projrep" obj_id="1" obj_type="1" name="PROJREP - MOVE APPROVED PROJECT BUDGET AFTER LOAD"/></deployobjects></HBRRepo>