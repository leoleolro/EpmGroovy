
<HBRRepo>
  <variables/>
  <rulesets/>
  <rules>
    <rule id="1" name="ProRep - Clear Weeks" product="Planning">
      <property name="application">RPEPM</property>
      <property name="plantype">Physical</property>
      <script type="groovy">
/* Rule Name : ProRep - Clear Weeks
   Purpose   : Clears rates data from Form 1.2 PayItem by cost code.
   Date Created: 31/07/2025
   Created By : SS
   Notes     : Uses clearPartialData on the Physical cube
*/

/* RTPS */

// Get Physical cube
Cube cube = operation.getApplication().getCube('Physical')

// Context: Clear specific POV combination
// Use Case: Removes data for Actual/Final, FY25, Load, Month, specific Entity/Project,
// BegBalance, Asset Meters, Mining Equipment, and all weeks
cube.clearPartialData(
    "CrossJoin(" +
        "CrossJoin(" +
            "CrossJoin(" +
                "CrossJoin(" +
                    "CrossJoin(" +
                        "CrossJoin(" +
                            "CrossJoin(" +
                                "CrossJoin(" +
                                    "CrossJoin({[Actual]},{[Final]})," +
                                    "{[FYxx]}" +          // Masked FY25
                                ")," +
                                "{[Load]}" +
                            ")," +
                            "{[Month]}" +
                        ")," +
                        "{[ENT_xxxxx]}" +        // Masked entity
                    ")," +
                    "{[PRJ_xxxxxx]}" +           // Masked project
                ")," +
                "{[BegBalance]}" +
            ")," +
            "{Descendants([Asset Meters], [Asset Meters].dimension.Levels(0))}" +
        ")," +
        "{Descendants([Mining_Equipment], [Mining_Equipment].dimension.Levels(0))}" +
    ")," +
    "{Descendants([Total Weeks], [Total Weeks].dimension.Levels(0))}" +
    ")", 
    true
)
      </script>
    </rule>
  </rules>
  <components/>
</HBRRepo>
