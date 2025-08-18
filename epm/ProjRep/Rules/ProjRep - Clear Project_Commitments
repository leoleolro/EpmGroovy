/* RTPS: {Var_Dim_Year},{Var_Dim_Period}  */

long startTime = currentTimeMillis()

/* Define cube where the calculations will take place */
Cube cube = operation.getApplication().getCube('ProjRep')

/* Define the data grid */
FlexibleDataGridDefinitionBuilder dataGrid = cube.flexibleDataGridDefinitionBuilder()

/*Populate the Datagrid*/
dataGrid.setPov('BegBalance','No Scenario','No View','No Year','No Line Item','Load','No Version') //set POV members
dataGrid.addColumn('Project Status') //set column members
dataGrid.addRow('ILvl0Descendants(All Entities)','ILvl0Descendants(All Projects)') //set row members

/*Grid suppression*/
dataGrid.setSuppressMissingRows(true)
dataGrid.setSuppressMissingBlocks(true)
dataGrid.setSuppressMissingSuppressesZero(true)

//List to Project Members
List&lt;String> ProjList = []
List&lt;String> ProjListString = []

// Load a data grid from the specified grid definition and cube
cube.loadGrid(dataGrid.build(), false).withCloseable { grid ->
 grid.dataCellIterator.each { cell ->
     if (cell.data == 2)
         ProjList.add(cell.getMemberName("Project"))
     		
                   }
}

if(ProjList.size()==0){

 println "Rule : ProjRep - Clear Project_Commitments | No Projects are Active. Exiting script. |Total Time Elapsed : ${(currentTimeMillis() - startTime) / 1000} Secs."

}
else{

/*Adding the Active Projects to a Collection*/
List&lt;String> ProjListFormatted = new ArrayList&lt;>(ProjList)

/*Creating the POV for the Partial Clear*/
String mdxRegion = getCrossJoins([['[Commitments]'],['[Load]'], ['[Month]'],['[Final]'],['Descendants([Total Tasks], [Total Tasks].dimension.Levels(0))'],ProjListFormatted,['Descendants([All Entities], [All Entities].dimension.Levels(0))'],['Descendants([Project Cost Codes], [Project Cost Codes].dimension.Levels(0))'],[mdxParams(rtps.Var_Dim_Year.member)],[mdxParams(rtps.Var_Dim_Period.member)]])
Boolean physical = false

try {
    cube.clearPartialData(mdxRegion, physical)
} catch (e) {
    println e.message
}
println "Rule : ProjRep - Clear Project_Commitments | Total Time Elapsed : ${(currentTimeMillis() - startTime) / 1000} Secs."
}
/*Custom function to used to create a cross-join. The function expects a list to be provided*/
String getCrossJoins(List&lt;List&lt;String>> essIntersection) {
    String crossJoinString
    if (essIntersection.size() > 1) {
        crossJoinString = essIntersection[1..-1].inject('{' + essIntersection[0].join(',') + '}') { concat, members -> "CrossJoin(" + concat + ',{' + members.join(',') + '})' }
    }
    return crossJoinString
}
</script></rule></rules><components/><deployobjects><deployobject product="2" application="rpepm" plantype="projrep" obj_id="1" obj_type="1" name="PROJREP - CLEAR PROJECT_COMMITMENTS"/></deployobjects></HBRRepo>