<?php

//Get params
$groupId 		= $_GET["id"];
$groupName 		= $_GET["name"];

//Get data
$jsonData = file_get_contents("data.json");
$data = json_decode($jsonData, true);

//Search and update
for($i = 0; $i<count($data["groups"]); $i++){
	$group = $data["groups"][$i];

	if($groupId == $group["id"]){
		//Update values

		if($groupName){
			$data["groups"][$i]["name"] = $groupName;
		}
	}
}

//Encode and return it
$jsonData = json_encode($data);
file_put_contents("data.json", $jsonData);
header('Content-Type: application/json');
echo $jsonData;





