
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
    <rule id="1" name="Physicals - Move Physicals to Stored Members" product="Planning">
      <property name="application">RPEPM</property>
      <property name="plantype">Physical</property>

      <variable_references>
        <variable_reference name="Var_Dim_Entity" id="1">
          <property name="hidden">false</property>
          <property name="rule_name">Physicals - Move Physicals to Stored Members</property>
          <property name="seq">1</property>
          <property name="type">3</property>
          <property name="useAsOverrideValue">false</property>
        </variable_reference>

        <variable_reference name="Var_Dim_Project" id="2">
          <property name="hidden">false</property>
          <property name="rule_name">Physicals - Move Physicals to Stored Members</property>
          <property name="seq">2</property>
          <property name="type">3</property>
          <property name="useAsOverrideValue">false</property>
        </variable_reference>
      </variable_references>

<script type="groovy">
/* Rule Name : Physicals - Move Physicals to Stats Cube
   Purpose : Move the measures loaded into the Physicals cube to the Stats Cube
*/

/* RTPS: {Var_Dim_Entity}, {Var_Dim_Project} */

// Context: Load application and cube references
Application app = operation.application
Cube cube = app.getCube("Physical")

// Context: Define list of all periods in order
List periodRange = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec']

// Use Case: Identify actual month from substitution variable
def actPd = operation.application.getSubstitutionVariableValue("&ActMonth")
def pdNum = periodRange.findIndexOf{ name -> name =~ actPd}

// Use Case: Build list of periods from Jan up to current Actual month
def actPds = periodRange[0..pdNum]
String actPdsStr = actPds.join(", ")
println "actPdsStr: $actPdsStr \n"

// **************************************************************************************
// Step 1: Create source grid from dynamic members
// **************************************************************************************
DataGridDefinitionBuilder sourceBuilder = cube.dataGridDefinitionBuilder()
sourceBuilder.setSuppressMissingRowsNative(true)

// Context: Set cube POV (Version, Scenario, View)
sourceBuilder.addPov([ 'Version', 'Scenario', 'View'],
                    [  ['Final'], ['Actual'], ['Month']  ] )

// Context: Set Year based on substitution variable
sourceBuilder.addColumn(['Year'], [ ["&ActYear"] ])

// Context: Define row structure with dynamic members (time, accounts, entity, project, etc.)
sourceBuilder.addRow([ 'Period' ,'Account', 'Entity', 'Asset', 'Plan Element', 'Day', 'Project'],
                       [
                       [actPdsStr],
                       ['ILvl0Descendants("Physicals Measures")'],
                       [mdxParams(rtps.Var_Dim_Entity.member)],
                       ['ILvl0Descendants("Mining_Equipment")'],
                       ['ILvl0Descendants("Total Plan")'],
                       ['Total Days'],
                       [mdxParams(rtps.Var_Dim_Project.member)]
                     ])
                      
DataGridDefinition sourceGridDef = sourceBuilder.build()
def targetRows = []

// **************************************************************************************
// Step 2: Map dynamic members to stored members
// **************************************************************************************
cube.loadGrid(sourceGridDef, false).withCloseable { sourceGrid ->

  // Context: Define mapping between dynamic and stored members
  Map mappings = [
                    'Total Days' : 'ALT_Total Days'
                 ]
                 
  // Use Case: Apply mappings row by row
  sourceGrid.rows.each { row ->
    List<String> rowHeaders = row.headers.collect{mappings.containsKey(it.mbrName) ? mappings[it.mbrName] : it.mbrName}
    def rowData = []
    rowData = row.data.collect{it.missing ? '#Missing' : it.data}
    targetRows << [rowHeaders, rowData]
  }
}

// **************************************************************************************
// Step 3: Create target grid and push transformed data into cube
// **************************************************************************************
if(targetRows){
 
  // Context: Build new data grid with mapped values
  DataGridBuilder targetGrid = cube.dataGridBuilder("MM/DD/YYYY") 
  targetGrid.addPov('Final', 'Actual', 'Month')
  targetGrid.addColumn ("&ActYear")
 
  targetRows.each {  row ->
     List listRow = (List) row
     targetGrid.addRow((List<String>) listRow[0] , (List<Double>) listRow[1])
  }
 
  DataGridBuilder.Status status = new DataGridBuilder.Status()
 
  targetGrid.build(status).withCloseable { grid ->
    println("Total number of cells accepted: $status.numAcceptedCells")
    println("Total number of cells rejected: $status.numRejectedCells")
    println("First 100 rejected cells: $status.cellsRejected")

    // Use Case: Persist mapped results into cube
    cube.saveGrid(grid)
  }
}

// **************************************************************************************
// Step 4: Push updated data into Stats Cube via Data Map
// **************************************************************************************
if(operation.application.hasDataMap("Physicals To Stats"))
	operation.application.getDataMap("Physicals To Stats").execute(true)

</script>
    </rule>
  </rules>

  <components/>

  <deployobjects>
    <deployobject product="2" application="rpepm" plantype="physical" obj_id="1" obj_type="1" name="PHYSICALS - MOVE PHYSICALS TO STORED MEMBERS"/>
  </deployobjects>
</HBRRepo>
