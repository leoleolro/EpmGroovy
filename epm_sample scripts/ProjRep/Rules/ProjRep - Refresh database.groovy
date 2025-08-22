<HBRRepo><variables/><rulesets/><rules><rule id="1" name="ProjRep - Refresh database" product="Planning"><property name="application">RPEPM</property><property name="plantype">ProjRep</property><script type="groovy">/* RTPS:  */

 HttpResponse&lt;String> jsonResponse1 = operation.application.getConnection("Pipeline").post()
    .body(json(["jobType":"pipeline", "jobName":"RV2"])).asString();
    println("Database Refreshed Successfully.")</script></rule></rules><components/><deployobjects><deployobject product="2" application="rpepm" plantype="projrep" obj_id="1" obj_type="1" name="PROJREP - REFRESH DATABASE"/></deployobjects></HBRRepo>