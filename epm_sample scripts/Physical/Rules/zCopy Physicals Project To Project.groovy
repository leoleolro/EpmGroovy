
<HBRRepo>
  <variables/>
  <rulesets/>
  <rules>
    <rule id="1" name="zCopy Physicals Project To Project" product="Planning">
      <property name="application">RPEPM</property>
      <property name="plantype">Physical</property>
      <script type="groovy">
/* RTPS:
   Context: Copy physical data from one project to another in the Physicals cube.
   Purpose: Automates the transfer of all members under PRJ_100300 to PRJ_100193
            for all dimensions including Days, Asset, Entity, Period, Year, Load, Actual, Final, Month.
*/

// === Initialize Custom Calculation Parameters ===
CustomCalcParameters parameters0 = new CustomCalcParameters()

// === Set the POV (Point of View) for the cube calculation ===
// Using nested CrossJoins to include all relevant dimensions
parameters0.Pov = "Crossjoin(" +
                  "Crossjoin(" +
                  "Crossjoin(" +
                  "Crossjoin(" +
                  "Crossjoin(" +
                  "Crossjoin(" +
                  "Crossjoin(" +
                  "Crossjoin(" +
                  "{Descendants([Physicals Tracking], [Physicals Tracking].dimension.Levels(0))}," +
                  "{Descendants([Asset], [Asset].dimension.Levels(0))})," +
                  "{Descendants([Day], [Day].dimension.Levels(0))})," +
                  "{Descendants([Entity], [Entity].dimension.Levels(0))})," +
                  "{Descendants([Period], [Period].dimension.Levels(0))})," +
                  "{[Load]})," +
                  "{[Actual]})," +
                  "{[Final]})," +
                  "{[Month]})," +
                  "{Descendants([Year], [Year].dimension.Levels(0))})"

// === No target cube, credit or debit members needed ===
parameters0.target = ""
parameters0.creditMember = ""
parameters0.debitMember = ""

// === Script: assign data from source project to target project ===
parameters0.script = "([PRJ_100193]) :=  ([PRJ_100300]);"

// === Source region: defines the data to copy ===
parameters0.sourceRegion = "{[PRJ_100300]}"

// === Rounding digits not used ===
parameters0.roundDigits = ""

// === Execute ASO cube calculation ===
operation.getApplication().getCube('Physical').executeAsoCustomCalculation(parameters0)

      </script>
    </rule>
  </rules>
  <components/>
  <deployobjects>
    <deployobject product="2" application="rpepm" plantype="physical" obj_id="1" obj_type="1" name="ZCOPY PHYSICALS PROJECT TO PROJECT"/>
  </deployobjects>
</HBRRepo>
