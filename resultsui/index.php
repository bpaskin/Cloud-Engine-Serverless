<?php
$url = $_SERVER['HTTP_HOST'];
$url = str_replace("resultsui", "resultsquery", $url);
?>

<html>
<head>
<title>Eurovision 2022 Results</title>
</head>
<body>
	<img
		src="https://static.eurovision.tv/dist/assets/images/esc/2022/logo-black.b9b5bfc57b81d725d184..svg">
	<h1>Results</h1>
	<p id="result"></p>
	<script>
			async function sendJSON(name){
				// Creating a XHR object
				let result = document.querySelector('.result');
				let xhr = new XMLHttpRequest();
                		
				// open a connection
				xhr.open("POST", "https://<?php print($url); ?>", true);
	
				// Set the request header i.e. which type of content you are sending
				xhr.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");

				// Create a state change callback
				xhr.onreadystatechange = function () {
					if (xhr.readyState === 4 && xhr.status === 200) {
                        console.log(name + ":" + this.responseText);
	
						// Print received data from server
						document.getElementById("result").innerHTML = document.getElementById("result").innerHTML + "<b>" + name + ":" +  xhr.responseText + "</b></br>";
					}
				};
	
				var data = 'country=' + name.toLowerCase();
				console.log(data);

				// Sending data with the request
				xhr.send(data);
			}
		
			const countries = ["Albania",
        		"Armenia",
        		"Australia",
        		"Austria",
		        "Azerbaijan",
		        "Belgium",
		        "Bulgaria",
		        "Croatia",
		        "Cyprus",
		        "CzechRepublic",
		        "Denmark",
		        "Estonia",
		        "Finland",
		        "France",
		        "Germany",
		        "Georgia",
		        "Greece",
		        "Iceland",
		        "Ireland",
		        "Israel",
		        "Italy",
		        "Latvia",
		        "Lithuania",
		        "Malta",
		        "Moldova",
		        "Montenegro",
		        "Netherlands",
		        "NorthMacedonia",
		        "Norway",
		        "Poland",
		        "Portugal",
		        "Romania",
		        "SanMarino",
		        "Serbia",
		        "Slovenia",
		        "Spain",
		        "Sweden",
		        "Switzerland",
		        "Ukraine",
		        "UnitedKingdom"];

            while ( countries.length > 0) {
                var country = countries.shift();
                sendJSON(country);
            }
            
		</script>
</body>
</html>
