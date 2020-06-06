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
			Rise wil be used for simulation
		</h2>
		<form id="simulate"  method="post">
			<%
			String wrongFile = (String) request.getAttribute("NotGen");
			if(wrongFile == null || !wrongFile.equals("0")){
			%>
			<h4 style="color: red;" style="padding-center: 0px;">No Make File was found</h4>
			 <BR/>
			 <table>
			 	<tr>
			 		<td>
			 			<input class="btn-get-started" type="submit" value="Try again" onclick="toWhere('ct.jsp')"/>
			 		</td>
			 		<td>
			 			<input class="btn-get-started" type="submit" value="Regenerate" onclick="toWhere('start.jsp')"/>
			 		</td>
			 	</tr>
			 </table>
			<%} else {%>
				<h4 style="padding-center:0px;">Model (makefile) is being generated</h4>
				<input class="btn-get-started" type="submit" value="simulate" onclick="toWhere('/simulate')"/>
			<%} %>
		</form>
	</div>
	</section>
	<div class="copyrights">
		<div class="container">
			<p align="center">The Advanced simulation Laboratory (ARS)<BR/>
			Department of Systems and Computer Engineering, Carleton University, Ottawa, Canada</p>
		</div>
	</div>
	<script>
	function changeCourseOfActoin(towhere) {
		document.getElementById("form_id").action = towhere;
		document.forms["theform"].submit();
	}
	</script>

</body>
</html>
