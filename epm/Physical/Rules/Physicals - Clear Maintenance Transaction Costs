
<HBRRepo>
  <variables>
    <variable name="Var_Dim_Period" type="member" usage="const" id="2" product="Planning">
      <property name="dimensionInputMode">type</property>
      <property name="dimensionType">Period</property>
      <property name="prompt_text">Please Select The Period</property>
      <property name="scope">ruleset</property>
      <property name="useLastValue">true</property>
      <value>"&amp;UV_Period"</value>
    </variable>

    <variable name="Var_Dim_Year" type="member" usage="const" id="1" product="Planning">
      <property name="dimensionInputMode">type</property>
      <property name="dimensionType">Year</property>
      <property name="prompt_text">Please Select The Year</property>
      <property name="scope">ruleset</property>
      <property name="useLastValue">true</property>
      <value>"&amp;UV_Year"</value>
    </variable>
  </variables>

  <rulesets/>

  <rules>
    <rule id="1" product="Planning">
      <variable_references>
        <variable_reference name="Var_Dim_Period" id="2"/>
        <variable_reference name="Var_Dim_Year" id="1"/>
      </variable_references>

      <script type="groovy">
/* RTPS: {Var_Dim_Year},{Var_Dim_Period} */

long startTime = currentTimeMillis()
// Context: Capture start time for performance logging

Cube cube = operation.getApplication().getCube('ProjRep')
// Context: Define the reporting cube where the script will query data
// Use Case: Used to fetch active projects from ProjRep cube

FlexibleDataGridDefinitionBuilder dataGrid = cube.flexibleDataGridDefinitionBuilder()
// Context: Create a flexible grid definition to query data

dataGrid.setPov('BegBalance','No Scenario','No View','No Year','No Line Item','Load','Working')
// Context: Set POV (Point of View) for the grid
// Use Case: Focus on specific slice of data for querying active projects

dataGrid.addColumn('Project Status')
// Context: Add Project Status as column member

dataGrid.addRow('ILvl0Descendants(All Entities)','ILvl0Descendants(All Projects)')
// Context: Add rows for all leaf-level entities and projects
// Use Case: Query across all entities/projects

// Grid suppression (performance optimization)
dataGrid.setSuppressMissingRows(true)
dataGrid.setSuppressMissingBlocks(true)
dataGrid.setSuppressMissingSuppressesZero(true)

List<String> ProjList = []
// Context: Will hold active project IDs
List<String> ProjListString = []

cube.loadGrid(dataGrid.build(), false).withCloseable { grid ->
    grid.dataCellIterator.each { cell ->
        if (cell.data == 2) // Context: Check for "active" project indicator
            ProjList.add(cell.getMemberName("Project"))
    }
}
// Use Case: Build list of active projects based on status

if(ProjList.size()==0){
    println "No Projects are Active. Exiting script. | Time Elapsed: ${(currentTimeMillis() - startTime) / 1000} Secs."
}
// Use Case: If no active projects exist, exit early to save processing time
else {
    List<String> ProjListFormatted = new ArrayList<>(ProjList)
    // Context: Convert project list into formatted collection for cross-join usage

    Cube ClearCube = operation.getApplication().getCube('Physical')
    // Context: Target cube where data will be cleared

    String mdxRegion = getCrossJoins([
        ['[Actual]'],
        ['[Load]'],
        ['[Month]'],
        ['[Final]'],
        ['[No Day]'],
        ProjListFormatted,
        ['Descendants([Equipment Costs], [Equipment Costs].dimension.Levels(0))'],
        ['Descendants([All Entities], [All Entities].dimension.Levels(0))'],
        ['Descendants([Mining_Equipment], [Mining_Equipment].dimension.Levels(0))'],
        [mdxParams(rtps.Var_Dim_Year.member)],
        [mdxParams(rtps.Var_Dim_Period.member)]
    ])
    // Context: Build MDX region string with cross-join function
    // Use Case: Define the exact slice of cube data to be cleared

    Boolean physical = false

    try {
        ClearCube.clearPartialData(mdxRegion, physical)
        // Context: Execute clear operation on specified MDX region
        // Use Case: Clears maintenance transaction costs for active projects
    } catch (e) {
        println e.message
    }

    println "Clear completed. | Time Elapsed: ${(currentTimeMillis() - startTime) / 1000} Secs."
}

// Custom function: Builds nested CrossJoin MDX expressions from list input
String getCrossJoins(List<List<String>> essIntersection) {
    String crossJoinString
    if (essIntersection.size() > 1) {
        crossJoinString = essIntersection[1..-1].inject('{' + essIntersection[0].join(',') + '}') { concat, members ->
            "CrossJoin(" + concat + ',{' + members.join(',') + '})'
        }
    }
    return crossJoinString
}
      </script>
    </rule>
  </rules>

  <components/>

  <deployobjects>
    <deployobject product="2" application="rpepm" plantype="physical" obj_id="1" obj_type="1"/>
  </deployobjects>
</HBRRepo>
