/* RTPS: {Var_Dim_Entity} {Var_Dim_Project}  */

def startTime = currentTimeMillis()

/* Define cube where the calculations will take place */
Cube cube = operation.getApplication().getCube('ProjRep')

/* Define the data grid */
def dataGrid = cube.flexibleDataGridDefinitionBuilder()

/*Populate the Datagrid*/
/*dataGrid.addRow(['Year' ,'Entity', 'Account', 'Project', 'Line Item', 'View'],
                       [
                       ['ILvl0Descendants("All Years")'],
                       ['ILvl0Descendants("All Entities")'],
                       ['ILvl0Descendants("Project Cost Codes")'],
                       ['ILvl0Descendants("All Projects")'],
                       ['ILvl0Descendants("Total Tasks")'],
                       ['LBUD_Jan','LBUD_Feb','LBUD_Mar','LBUD_Apr','LBUD_May','LBUD_Jun','LBUD_Jul','LBUD_Aug','LBUD_Sep','LBUD_Oct','LBUD_Nov','LBUD_Dec'] 
                        
                     ])*/
dataGrid.setPov('BegBalance','FinancePlan','Final') //FlexibledatagridBuilder so you don't need to specify dimension
dataGrid.addColumn('Load')
dataGrid.addRow(
    'ILvl0Descendants("All Years")',
    'ILvl0Descendants("All Entities")',
    'ILvl0Descendants("Project Cost Codes")',
    'ILvl0Descendants("All Projects")',
    'ILvl0Descendants("Total Tasks")',
    ['LBUD_Jan','LBUD_Feb','LBUD_Mar','LBUD_Apr','LBUD_May','LBUD_Jun','LBUD_Jul','LBUD_Aug','LBUD_Sep','LBUD_Oct','LBUD_Nov','LBUD_Dec']
)               

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

println ProjList

if(ProjList.size()==0){

 println "Rule : ProjRep - Clear Project_Approved_Budgets | No Projects are Active. Exiting script. |Total Time Elapsed : ${(currentTimeMillis() - startTime) / 1000} Secs."

}
else{

/*Adding the Active Projects to a Collection*/
def ProjListFormatted = ProjList.collect { [it] }
//def ProjEntityFormatted ="["+ rtps.Var_Dim_Entity.member +"]"

/*Creating the POV for the Partial Clear*/
/*String mdxRegion = getCrossJoins([
    ['Load'], // Plan Element
    ['BegBalance'], // Period
    ['Final'], // Version (assuming 'Final' maps to 'No Version' in your initial POV)
    ['FinancePlan'],
    ['ILvl0Descendants("All Years")'],
    ['ILvl0Descendants("All Entities")'],
    ['ILvl0Descendants("Project Cost Codes")'],
    ['ILvl0Descendants("All Projects")'],
    ['ILvl0Descendants("Total Tasks")'],
    ['LBUD_Jan','LBUD_Feb','LBUD_Mar','LBUD_Apr','LBUD_May','LBUD_Jun','LBUD_Jul','LBUD_Aug','LBUD_Sep','LBUD_Oct','LBUD_Nov','LBUD_Dec'] 
])*/

/*String mdxRegion = getCrossJoins([
    ['[Load]'], // Plan Element
    ['[BegBalance]'], // Period
    ['[Final]'], // Version (assuming 'Final' maps to 'No Version' in your initial POV)
    ['[FinancePlan]'],
    ['ILvl0Descendants("All Years")'],
    ['ILvl0Descendants("All Entities")'],
    ['ILvl0Descendants("Project Cost Codes")'],
    ['ILvl0Descendants("All Projects")'],
    ['ILvl0Descendants("Total Tasks")'],
    ['LBUD_Jan','LBUD_Feb','LBUD_Mar','LBUD_Apr','LBUD_May','LBUD_Jun','LBUD_Jul','LBUD_Aug','LBUD_Sep','LBUD_Oct','LBUD_Nov','LBUD_Dec'] 
])*/

String mdxRegion = getCrossJoins([
    ['[Load]'], // Plan Element
    ['[BegBalance]'], // Period
    ['[Final]'], // Version (assuming 'Final' maps to 'No Version' in your initial POV)
    ['[FinancePlan]'],
    ['[COM_7]'],
    ['[PRJ100182]'],
    ['[FY25]'],
    ['[Architectural Finishes]'],
    ['[ENT_41105]'],
    ['LBUD_Jan','LBUD_Feb','LBUD_Mar','LBUD_Apr','LBUD_May','LBUD_Jun','LBUD_Jul','LBUD_Aug','LBUD_Sep','LBUD_Oct','LBUD_Nov','LBUD_Dec'] 
])


Boolean physical = true

try {
    cube.clearPartialData(mdxRegion, physical)
} catch (e) {
    println e.message
}
println "Rule : ProjRep - Clear Project_Approved_Budgets | Total Time Elapsed : ${(currentTimeMillis() - startTime) / 1000} Secs."
}
/*Custom function to used to create a cross-join. The function expects a list to be provided*/
String getCrossJoins(List&lt;List&lt;String>> essIntersection) {
    String crossJoinString
    if (essIntersection.size() > 1) {
        crossJoinString = essIntersection[1..-1].inject('{' + essIntersection[0].join(',') + '}') { concat, members -> "CrossJoin(" + concat + ',{' + members.join(',') + '})' }
    }
    return crossJoinString
}
</script></rule></rules><components/><deployobjects><deployobject product="2" application="rpepm" plantype="projrep" obj_id="1" obj_type="1" name="PROJREP - CLEAR PROJECT APPROVED BUDGETS STAGING"/></deployobjects></HBRRepo>