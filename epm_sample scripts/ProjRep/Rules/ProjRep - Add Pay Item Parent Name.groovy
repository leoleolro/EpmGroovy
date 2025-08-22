
<HBRRepo>
  <variables>
    <variable name="Var_Source_File_Name" type="str" usage="const" id="1" product="Planning">
      <property name="prompt_text">Please enter the source Contract Line and Pay Item files</property>
      <property name="scope">ruleset</property>
      <value/>
    </variable>
  </variables>
  <rulesets/>
  <rules>
    <rule id="1" name="ProjRep - Add Pay Item Parent Name" product="Planning">
      <property name="application">RPEPM</property>
      <property name="plantype">ProjRep</property>
      <variable_references>
        <variable_reference name="Var_Source_File_Name" id="1">
          <property name="hidden">false</property>
          <property name="rule_name">ProjRep - Add Pay Item Parent Name</property>
          <property name="seq">1</property>
          <property name="type">0</property>
          <property name="useAsOverrideValue">false</property>
        </variable_reference>
      </variable_references>
      <script type="groovy">
/* 
  Rule Name : ProjRep - Add Pay Item Parent Name
  Purpose   : Adds a "Pay Item Parent Name" row to the integration CSV file.
              Specifically, inserts a "ROOT" row after the header with project info.
  Date Created: 08/05/2025
  Created By: Sneha Singh
*/

// === Get file name from input variable ===
String strFileName = rtps.Var_Source_File_Name.toString()

// Extract Project Name from the file name (assumes first segment before '_' is the project)
String strProjectName = strFileName.split("_")[0]
println("Project Name: " + strProjectName)

// === Storage for transformed CSV rows ===
def transformedData = []

// Flags to handle header row and extra inserted row
boolean isFirstRow = true
boolean secondRowInserted = false  

// === Step 1: Read and transform CSV ===
csvIterator(strFileName).withCloseable { inputFileRows ->
    inputFileRows.eachWithIndex { inputFileRow, index ->

        // Convert row to List<String>
        List<String> transformedRow = inputFileRow as List<String>

        if (isFirstRow) {
            // Keep header row unchanged
            transformedData << transformedRow
            isFirstRow = false
        } else {
            // Insert "ROOT" row after header (as second row)
            if (!secondRowInserted) {
                def newRow = new ArrayList<>(Collections.nCopies(transformedRow.size(), "")) // blank row with same columns
                newRow[0] = "ROOT"             // line_number column
                newRow[2] = "PI"               // parent column
                newRow[13] = strProjectName    // proj_number column (14th column, index 13)
                transformedData << newRow
                secondRowInserted = true
            }

            // Append original data row unchanged
            transformedData << transformedRow
        }
    }
}

// === Step 2: Overwrite original file with transformed rows ===
csvWriter(strFileName).withCloseable { writer ->
  transformedData.each { row ->
    writer.writeNext(row as String[])
  }
}

      </script>
    </rule>
  </rules>
  <components/>
  <deployobjects>
    <deployobject product="2" application="rpepm" plantype="projrep" obj_id="1" obj_type="1" name="PROJREP - ADD PAY ITEM PARENT NAME"/>
  </deployobjects>
</HBRRepo>
