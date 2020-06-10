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
		<form name="theform" id="simulate" action="simulate" method="post">
			<script>
			function changDest(towhere) {
				alert(towhere);
				document.forms[0].action = towhere;
			    document.forms[0].submit();
			}
			</script>
			<%
			String ct = "start.jsp";
			String wrongFile = (String) request.getAttribute("NotGen");
			if(wrongFile != null && !wrongFile.equals("0")){
			%>
			<h4 style="color: red;" style="padding-center: 0px;">No Make File was found</h4>
			 <BR/>
			 <table>
			 	<tr>
			 		<td>
			 			<input class="btn-get-started" type="submit" value="Try again"/>
			 		</td>
			 		<td>
			 			<input class="btn-get-started" type="button" value="Regenerate" onclick="changDest('start.jsp')"/>
			 		</td>
			 	</tr>
			 </table>
			<%} else {%>
				<table>
					<tr>
						<td align="left" >
							<h3>Number of occupants</h3><input id="occupants" name="occupants" type="text" 	value="5" size="10" />
						</td>
					</tr>
					<tr>
						<td align="left">
							<h3>Windows Closed?</h3><input type="checkbox" id="closed" name="closed" />
						</td>
					</tr>
				</table>
				<br/> <br/>
			
				<h4 style="padding-center:0px;">Model (makefile) is being generated</h4>
				<input class="btn-get-started" type="submit" value="simulate"/>
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
</body>
</html>
