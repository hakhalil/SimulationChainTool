<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html>
<head>
<!--title>Tab Title</title-->

<link
	href="https://fonts.googleapis.com/css?family=Open+Sans:300,300i,400,400i,700,700i|Roboto:100,300,400,500,700|Philosopher:400,400i,700,700i" rel="stylesheet">
<link href="lib/bootstrap/css/bootstrap.min.css" rel="stylesheet">
<link href="css/style.css" rel="stylesheet">

</head>

<body>

	<section id="hero" class="wow fadeIn">
	<div class="hero-container">
		<h1>Integrated Simulation and Visualization Tool</h1>
		<img src="img/logo.JPG" width="330px" height="150px" >

		<h2 style="padding-center: 0px;">
			From a Revit File (or Image) you will can create a model, then<br />
			run the simulation on the resulting model. Finally the log file is sent to the visualization
		</h2>
		<%
			String wrongFile = (String) request.getAttribute("WrongFile");
			request.removeAttribute("WrongFile");
			if(wrongFile != null && wrongFile.equals("1")){
		%>
		<h4 style="color: red;" style="padding-center: 0px;">Upload a correct file</h4>
		<BR />
		<%	} %>
		<form name="theform" action="userInputServlet" method="post" enctype="multipart/form-data">
		
			<h4 style="padding-center: 0px;">Upload a Revit or Image file:</h4>
			<input  class="btn-get-started" type="file" name="file" size="50" />
	
			<h4>Room width (in meters)</h4>
			<input id="rmWidth" name="rmWidth" type="text" 	value="0" size="10" /> 
			<br/> <br/>
			<input type="button" class="btn-get-started" value="Generate" onclick="validate()" />
		</form>
	</div>
	</section>
	<script>
		function validate() {
			if (document.getElementById("rmWidth").value > 0)
				document.forms["theform"].submit();
			else
				alert("Enter all the required information!");
		}
	</script>
	<div class="copyrights">
		<div class="container">
			<p align="center">The Advanced simulation Laboratory (ARS)<BR/>
			Department of Systems and Computer Engineering, Carleton University, Ottawa, Canada</p>
		</div>
	</div>
</body>
</html>
