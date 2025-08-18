
<HBRRepo>
  <variables>
    <variable name="Var_Dim_Entity" type="member" usage="const" id="1" product="Planning">
      <property name="dimensionInputMode">type</property>
      <property name="dimensionType">Entity</property>
      <property name="prompt_text">Please Select The Entity</property>
      <property name="scope">ruleset</property>
      <value/>
    </variable>

    <variable name="Var_Dim_Project" type="member" usage="const" id="3" product="Planning">
      <property name="dimension">Project</property>
      <property name="dimensionInputMode">name</property>
      <property name="prompt_text">Please Select the Project</property>
      <property name="scope">ruleset</property>
      <value/>
    </variable>

    <variable name="Var_Dim_Year" type="member" usage="const" id="2" product="Planning">
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
        <variable_reference name="Var_Dim_Entity" id="1"/>
        <variable_reference name="Var_Dim_Year" id="2"/>
        <variable_reference name="Var_Dim_Project" id="3"/>
      </variable_references>

      <script type="groovy">
/* RTPS: {Var_Dim_Entity}{Var_Dim_Year}{Var_Dim_Project} */

CustomCalcParameters parameters0 = new CustomCalcParameters()
// Context: Create a new instance of CustomCalcParameters to configure the calculation

parameters0.Pov = "Crossjoin(Crossjoin(Crossjoin(Crossjoin(Crossjoin(Crossjoin(Crossjoin(Crossjoin(Crossjoin(
    {Descendants([Physicals Measures], [Physicals Measures].dimension.Levels(0))}, 
    {Descendants([Mining_Equipment], [Mining_Equipment].dimension.Levels(0))}), 
    {${mdxParams(rtps.Var_Dim_Entity)}}), 
    {Descendants([YearTotal], [YearTotal].dimension.Levels(0))}), 
    {Descendants([Total Plan], [Total Plan].dimension.Levels(0))}), 
    {${mdxParams(rtps.Var_Dim_Project)}}), 
    {[Actual]}), 
    {[Final]}), 
    {[Month]}), 
    {${mdxParams(rtps.Var_Dim_Year)}})"
// Context: Define the Point of View (POV) for the calculation using nested Crossjoin functions
// Use Case: Combines multiple dimensions (Measures, Equipment, Entity, Year, Plan, Project, etc.) into a single calculation scope

parameters0.target = ""
// Context: No explicit target member defined

parameters0.creditMember = ""
parameters0.debitMember = ""
// Context: Credit and debit members not defined in this calculation

parameters0.script = "([N/A Days]) := ([Total Days]);"
// Context: The core script logic
// Use Case: Assigns the value of [Total Days] to [N/A Days]

parameters0.offset = ""
parameters0.sourceRegion = "{[Total Days]}"
// Context: Defines the source region of the calculation
// Use Case: Source region is limited to [Total Days]

parameters0.roundDigits = ""
// Context: No rounding applied

operation.getApplication().getCube('Physical').executeAsoCustomCalculation(parameters0)
// Context: Execute the custom calculation on the 'Physical' cube
// Use Case: Applies the defined calculation logic within the ASO cube
      </script>
    </rule>
  </rules>

  <components/>

  <deployobjects>
    <deployobject product="2" application="rpepm" plantype="physical" obj_id="1" obj_type="1"/>
  </deployobjects>
</HBRRepo>
