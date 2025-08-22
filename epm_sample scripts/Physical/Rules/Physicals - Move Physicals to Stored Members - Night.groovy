
<HBRRepo>
  <variables/>
  <rulesets/>
  <rules>
    <rule id="1" name="Physicals - Move Physicals to Stored Members - Night" product="Planning">
      <property name="application">RPEPM</property>
      <property name="plantype">Physical</property>
      <script type="groovy">
/* Rule Name : Physicals - Move Physicals to Stats Cube
Purpose : Move the measures loaded into the Physicals to the Stats Cube 
*/

/* RTPS: */

/**************************************************************************************/
/* Use Case: Copy dynamic members (calculated at runtime) into stored members
   so that values are persisted and available for downstream reporting or mappings.  */
/**************************************************************************************/

Application app = operation.application
Cube cube = app.getCube("Physical")

// Context: Define the ordered list of periods in a year
List periodRange = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec']

// Context: Get actual month from substitution variable
def actPd = operation.application.getSubstitutionVariableValue("&amp;ActMonth")

// Context: Find index of the actual month in the list
def pdNum = periodRange.findIndexOf{ name -> name =~ actPd}

// Context: Build range of months from Jan up to the current actual month
def actPds = periodRange[0..pdNum]
String actPdsStr = actPds.join(", ")

println "actPdsStr: $actPdsStr \n"

// Context: Previously supported project-based filtering, commented out
/* def ProjectsDim=app.getDimension("Project", cube)
def usrProjectMbrs = ProjectsDim.getEvaluatedMembers("""IDESCENDANTS(${rtps.Var_Dim_Project.toString()})""" as String, cube)
println "usrProjectMbrs: $usrProjectMbrs \n" */

/**************************************************************************************/
/* Step 1: Build a data grid that pulls dynamic members                               */
/**************************************************************************************/

DataGridDefinitionBuilder sourceBuilder = cube.dataGridDefinitionBuilder()

// Context: Suppress missing rows, blocks, and zero values for efficiency
sourceBuilder.setSuppressMissingRowsNative(true)
sourceBuilder.setSuppressMissingBlocks(true)
sourceBuilder.setSuppressMissingSuppressesZero(true)

// Context: POV members fixed across the grid
sourceBuilder.addPov([ 'Version', 'Scenario', 'View'],
                     [ ['Final'], ['Actual'], ['Month'] ] )

// Context: Add year dynamically using substitution variable
sourceBuilder.addColumn(['Year'], [ ["&amp;ActYear"] ])

// Context: Define row structure with dynamic members for period, account, entity, etc.
sourceBuilder.addRow([ 'Period' ,'Account', 'Entity', 'Asset', 'Plan Element', 'Day', 'Project'],
                     [
                       ['ILvl0Descendants("YearTotal")'],
                       ['ILvl0Descendants("Physicals Measures")'],
                       ['ILvl0Descendants("All Entities")'],
                       ['ILvl0Descendants("Mining_Equipment")'],
                       ['ILvl0Descendants("Total Plan")'],
                       ['Total Days'],
                       ['ILvl0Descendants("Operational Projects")']
                     ])

DataGridDefinition sourceGridDef = sourceBuilder.build()
def targetRows = []

/**************************************************************************************/
/* Step 2: Apply mappings between dynamic members and stored members                  */
/**************************************************************************************/

cube.loadGrid(sourceGridDef, false).withCloseable { sourceGrid ->

  // Use Case: Map "Total Days" to a stored member alternative "ALT_Total Days"
  Map mappings = [
    'Total Days' : 'ALT_Total Days',
  ]

  // Context: Transform row headers and collect data
  sourceGrid.rows.each { row ->
    List&lt;String> rowHeaders = row.headers.collect{mappings.containsKey(it.mbrName) ? mappings[it.mbrName] : it.mbrName}
    def rowData = []
    rowData = row.data.collect{it.missing ? '#Missing' : it.data}
    targetRows &lt;&lt; [rowHeaders, rowData]
  }
}

/******************************************************************************************/
/* Step 3: Build target grid and save transformed data back to cube                      */
/******************************************************************************************/

if(targetRows){

  // Context: Define new target grid to load mapped data
  DataGridBuilder targetGrid = cube.dataGridBuilder("MM/DD/YYYY")
  targetGrid.addPov('Final', 'Actual', 'Month')
  targetGrid.addColumn ("&amp;ActYear")

  // Context: Insert each row of transformed data
  targetRows.each { row ->
    List listRow = (List) row
    targetGrid.addRow((List&lt; String>) listRow[0] , (List&lt;Double>) listRow[1])
  }

  DataGridBuilder.Status status = new DataGridBuilder.Status()

  targetGrid.build(status).withCloseable { grid ->
    println("Total number of cells accepted: $status.numAcceptedCells")
    println("Total number of cells rejected: $status.numRejectedCells")
    println("First 100 rejected cells: $status.cellsRejected")

    // Use Case: Commit data changes to the cube
    cube.saveGrid(grid)
  }
}

/******************************************************************************************/
/* Step 4: Push the transformed data to the Stats cube using a DataMap                  */
/******************************************************************************************/

if(operation.application.hasDataMap("Physicals To Stats"))
  operation.application.getDataMap("Physicals To Stats").execute(true)

</script>
    </rule>
  </rules>
  <components/>
  <deployobjects>
    <deployobject product="2" application="rpepm" plantype="physical" obj_id="1" obj_type="1" name="PHYSICALS - MOVE PHYSICALS TO STORED MEMBERS - NIGHT"/>
  </deployobjects>
</HBRRepo>
