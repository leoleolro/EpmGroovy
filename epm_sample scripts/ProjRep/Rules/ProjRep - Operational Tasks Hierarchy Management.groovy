/* Get the cube where the calculations will take place*/
Cube cube = operation.getApplication().getCube('ProjRep')

Dimension dim = operation.application.getDimension("Line Item", cube)
List&lt;Member> mbrs = dim.getEvaluatedMembers("@RELATIVE(\"Operational Tasks\",0)", cube)

mbrs.each { mbr ->
	Map mbrProperties = mbr.toMap()

	// Extract and store the "Parent" property
	mbrProperties.each { k, v ->
      if (k == "Parent" &amp;&amp; v == "Operational Tasks") {
          mbr.delete()
      }
	}
}


</script></rule></rules><components/><deployobjects><deployobject product="2" application="rpepm" plantype="projrep" obj_id="1" obj_type="1" name="PROJREP - OPERATIONAL TASKS HIERARCHY MANAGEMENT"/></deployobjects></HBRRepo>