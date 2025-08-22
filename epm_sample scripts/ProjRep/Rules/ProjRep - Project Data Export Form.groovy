Rule Name : ProjRep - Project Data Export Form
Purpose : Used to Export the Project data 

/*RTPS: {Var_Dim_Entity}*/
Cube cube = operation.application.getCube("ProjRep") //set cube
def dataGrid = cube.flexibleDataGridDefinitionBuilder() //set grid def obj
dataGrid.setPov('{Var_Dim_Entity}',"No Scenario","No Version","No Year","Load","No View","BegBalance","No Line Item") //set POV members
dataGrid.addColumn('Descendants(Project Properties)') //set column members
dataGrid.addRow('ILvl0Descendants(All Projects)')  //set row members
//set suppression settings
dataGrid.setSuppressMissingRows(true)
dataGrid.setSuppressMissingBlocks(true)
dataGrid.setSuppressMissingSuppressesZero(true)
//load grid to memory
DataGrid gridToExport = cube.loadGrid(dataGrid.build(),false)
operation.exportDataToFile(gridToExport,'export_RP.csv') //export grid
</script></rule></rules><components/><deployobjects><deployobject product="2" application="rpepm" plantype="projrep" obj_id="1" obj_type="1" name="PROJREP - PROJECT DATA EXPORT FORM"/></deployobjects></HBRRepo>