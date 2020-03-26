<?php

//Get params
$id = $_GET["id"];

//Get data
$jsonData = file_get_contents("data.json");
$data = json_decode($jsonData, true);
$groups = $data["groups"];

//Update data
for($i=0; $i<count($data["groups"]); $i++){
	$group = $data["groups"][$i];

	for($j=0; $j<count($group["reminders"]); $j++){
		$reminder = $group["reminders"][$j];
		if($reminder["id"] == $id){
			unset($data["groups"][$i]["reminders"][$j]);
			$data["groups"][$i]["reminders"] = array_values($data["groups"][$i]["reminders"]);
		}
	}
}


//Encode and return it
$jsonData = json_encode($data);
file_put_contents("data.json", $jsonData);
header('Content-Type: application/json');
echo $jsonData;





