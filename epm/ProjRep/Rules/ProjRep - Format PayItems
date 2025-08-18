
Purpose : Used to Format pay Items for data Integration


/*RTPS: */
String Project_PayItem_Source_FileName = 'Cannington_Project.txt'
String Project_PayItem_Target_FileName = 'Project_Target.txt'

// Initialize the CSV writer for the target file
csvWriter(Project_PayItem_Target_FileName).withCloseable() { outputFileRows ->
    // Initialize the CSV reader for the source file
    csvIterator(Project_PayItem_Source_FileName, '|').withCloseable() { rows ->
        rows.eachWithIndex { row, idx ->
            try {

                // Trim each element in the row and convert it to a list
                List&lt;String> fileRecord = row.collect { it.toString().trim() } as List&lt;String>

                // Check if the row has the required number of columns
                if (fileRecord.size() &lt; 15) {
                    println "Skipping row $idx: Not enough columns."
                    return
                }

                // Extract specific columns into named variables
                def (String Entity, String Proj_Number, String Contract_Number, String Line_Number, String Data) = 
                    [fileRecord[15], fileRecord[14], fileRecord[11], fileRecord[1], fileRecord[15]]

                // Prepare the reformatted row for output
                List&lt;String> reformattedRow = [Entity, Proj_Number, Contract_Number, Line_Number, Data]

                // Write the reformatted row to the target file
                outputFileRows.writeNext(reformattedRow)

            } catch (Exception e) {
                println "Error processing row $idx: ${e.message}"
            }
        }
    }
}

println "Data successfully processed and written to $Project_PayItem_Target_FileName"
</script></rule></rules><components/><deployobjects><deployobject product="2" application="rpepm" plantype="projrep" obj_id="1" obj_type="1" name="PROJREP - FORMAT PAYITEMS"/></deployobjects></HBRRepo>