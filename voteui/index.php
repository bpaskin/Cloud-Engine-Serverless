<?php
  $url = $_SERVER['HTTP_HOST'];
  $url = str_replace("acceptvote", "voteui", $url);
?>
		
<html>
	<head>
		<title>Eurovision Voting 2022</title>
	</head>
	<body>
		<img src="https://static.eurovision.tv/dist/assets/images/esc/2022/logo-black.b9b5bfc57b81d725d184..svg">
		<H1>Make your Eurovision Song Contest Selection</h1>
		<form action="<?php print($url); ?>" method="POST">
			<select name='selection'>
				<option value='Albania'>Albania</option>
				<option value='Armenia'>Armenia</option>
				<option value='Australia'>Australia</option>
				<option value='Austria'>Austria</option>
				<option value='Azerbaijan'>Azerbaijan</option>
				<option value='Belgium'>Belgium</option>
				<option value='Bulgaria'>Bulgaria</option>
				<option value='Croatia'>Croatia</option>
				<option value='Cyprus'>Cyprus</option>
				<option value='CzechRepublic'>Czech Republic</option>
				<option value='Denmark'>Denmark</option>
				<option value='Estonia'>Estonia</option>
				<option value='Finland'>Finland</option>
				<option value='France'>France</option>
				<option value='Germany'>Germany</option>
				<option value='Georgia'>Georgia</option>
				<option value='Greece'>Greece</option>
				<option value='Iceland'>Iceland</option>
				<option value='Ireland'>Ireland</option>
				<option value='Israel'>Israel</option>
				<option value='Italy'>Italy</option>
				<option value='Latvia'>Latvia</option>
				<option value='Lithuania'>Lithuania</option>
				<option value='Malta'>Malta</option>
				<option value='Moldova'>Moldova</option>
				<option value='Montenegro'>Montenegro</option>
				<option value='Netherlands'>Netherlands</option>
				<option value='NorthMacedonia'>North Macedonia</option>
				<option value='Norway'>Norway</option>
				<option value='Poland'>Poland</option>
				<option value='Portugal'>Portugal</option>
				<option value='Romania'>Romania</option>
				<option value='SanMarino'>San Marino</option>
				<option value='Serbia'>Serbia</option>
				<option value='Slovenia'>Slovenia</option>
				<option value='Spain'>Spain</option>
				<option value='Sweden'>Sweden</option>
				<option value='Switzerland'>Switzerland</option>
				<option value='Ukraine'>Ukraine</option>
				<option value='UnitedKingdom'>United Kingdom</option>
			</select>
			<input type="submit">
		</form>
	</body>
</html>
