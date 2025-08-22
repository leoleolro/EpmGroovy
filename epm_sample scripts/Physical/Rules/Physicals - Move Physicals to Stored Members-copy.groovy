
<HBRRepo>
  <variables>
    <variable name="Var_Dim_Entity" type="member" usage="const" id="1" product="Planning">
      <property name="dimensionInputMode">type</property>
      <property name="dimensionType">Entity</property>
      <property name="prompt_text">Please Select The Entity</property>
      <property name="scope">ruleset</property>
      <value/>
    </variable>
    <variable name="Var_Dim_Project" type="member" usage="const" id="2" product="Planning">
      <property name="dimension">Project</property>
      <property name="dimensionInputMode">name</property>
      <property name="prompt_text">Please Select the Project</property>
      <property name="scope">ruleset</property>
      <value/>
    </variable>
  </variables>

  <rulesets/>

  <rules>
    <rule id="1" name="Physicals - Move Physicals to Stored Members-copy" product="Planning">
      <property name="application">RPEPM</property>
      <property name="plantype">Physical</property>

      <variable_references>
        <variable_reference name="Var_Dim_Entity" id="1">
          <property name="hidden">false</property>
          <property name="rule_name">Physicals - Move Physicals to Stored Members-copy</property>
          <property name="seq">1</property>
          <property name="type">3</property>
          <property name="useAsOverrideValue">false</property>
        </variable_reference>
        <variable_reference name="Var_Dim_Project" id="2">
          <property name="hidden">false</property>
          <property name="rule_name">Physicals - Move Physicals to Stored Members-copy</property>
          <property name="seq">2</property>
          <property name="type">3</property>
          <property name="useAsOverrideValue">false</property>
        </variable_reference>
      </variable_references>

      <script type="groovy">
/* Cleansed Metadata */

/* RTPS: {Var_Dim_Entity}, {Var_Dim_Project}*/

/**************************************************************************************/
/* Use Case: Move measures from Physicals cube into Stats cube, mapping dynamic
   members into stored members for improved performance and historical tracking.
/**************************************************************************************/
 
 Application app = operation.application
 Cube cube = app.getCube("Physical") // Context: reference to the "Physical" cube
 
// Context: Build a list of periods up to the current Actual month
List periodRange = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec']
def actPd = operation.application.getSubstitutionVariableValue("&amp;ActMonth") 
def pdNum = periodRange.findIndexOf{ name -> name =~ actPd}
def actPds = periodRange[0..pdNum]
String actPdsStr = actPds.join(", ")
println "actPdsStr: $actPdsStr \n"

// Context: Optional code for filtering projects by user input (commented out)
/*
def ProjectsDim=app.getDimension("Project", cube)
def usrProjectMbrs = ProjectsDim.getEvaluatedMembers("""IDESCENDANTS(${rtps.Var_Dim_Project.toString()})""" as String, cube)
println "usrProjectMbrs: $usrProjectMbrs \n"
*/
 
/**************************************************************************************/
/* Step 1: Create a grid definition to extract data from dynamic members              
/**************************************************************************************/
 
DataGridDefinitionBuilder sourceBuilder = cube.dataGridDefinitionBuilder()
sourceBuilder.setSuppressMissingRowsNative(true)
 
sourceBuilder.addPov([ 'Version', 'Scenario', 'View'],
                    [  ['Final'], ['Actual'], ['Month']  ] )
 
sourceBuilder.addColumn(['Year'], [ ["&amp;ActYear"] ])
 
// Context: Define rows across multiple dimensions
sourceBuilder.addRow([ 'Period' ,'Account', 'Entity', 'Asset', 'Plan Element', 'Day', 'Project'],
                       [
                       [actPdsStr], // Context: dynamic actual periods
                       ['ILvl0Descendants("Physicals Measures")'], 
                       [mdxParams(rtps.Var_Dim_Entity.member)], 
                       ['ILvl0Descendants("Mining_Equipment")'], 
                       ['ILvl0Descendants("Total Plan")'], 
                       ['Total Days'], 
                       [mdxParams(rtps.Var_Dim_Project.member)]
                     ])
                      
DataGridDefinition sourceGridDef = sourceBuilder.build()
def targetRows = []
 
/**************************************************************************************/
/* Step 2: Map dynamic members to stored members                                      */
/**************************************************************************************/
cube.loadGrid(sourceGridDef, false).withCloseable { sourceGrid ->
 
  Map mappings = [
    // Use Case: Map "Total Days" (dynamic) into "ALT_Total Days" (stored)
    'Total Days' : 'ALT_Total Days',  
  ]
                 
  sourceGrid.rows.each { row ->
    List&lt;String> rowHeaders = row.headers.collect{
      mappings.containsKey(it.mbrName) ? mappings[it.mbrName] : it.mbrName
    }
    def rowData = []
    rowData = row.data.collect{it.missing ? '#Missing' : it.data}
    targetRows &lt;&lt; [rowHeaders, rowData]
  }
}
 
/******************************************************************************************/
/* Step 3: Build a target grid and push transformed data back into the cube            
/******************************************************************************************/
 
if(targetRows){ // Context: only proceed if source grid contains data
 
  DataGridBuilder targetGrid = cube.dataGridBuilder("MM/DD/YYYY") 
  targetGrid.addPov('Final', 'Actual', 'Month')
  targetGrid.addColumn ("&amp;ActYear")
 
  targetRows.each {  row ->
    List listRow = (List) row
    targetGrid.addRow((List&lt; String>) listRow[0] , (List&lt;Double>) listRow[1])
  }
 
  DataGridBuilder.Status status = new DataGridBuilder.Status()
 
  targetGrid.build(status).withCloseable { grid ->
    println("Total number of cells accepted: $status.numAcceptedCells")
    println("Total number of cells rejected: $status.numRejectedCells")
    println("First 100 rejected cells: $status.cellsRejected")
 
    // Use Case: Commit the grid data back into cube storage
    cube.saveGrid(grid)
  }
}
 
/******************************************************************************************/
/* Step 4: Push the updated data to the STATS cube via a Data Map                       
/******************************************************************************************/
if(operation.application.hasDataMap("Physicals To Stats"))
	operation.application.getDataMap("Physicals To Stats").execute(true)

</script>
    </rule>
  </rules>

  <components/>

  <deployobjects>
    <deployobject product="2" application="rpepm" plantype="physical" obj_id="1" obj_type="1" 
      name="PHYSICALS - MOVE PHYSICALS TO STORED MEMBERS-COPY"/>
  </deployobjects>
</HBRRepo>
