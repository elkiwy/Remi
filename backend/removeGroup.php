<?php

//Get params
$groupId = $_GET["id"];

//Get data
$jsonData = file_get_contents("data.json");
$data = json_decode($jsonData, true);
$groups = $data["groups"];

//Update data
for($i=0; $i<count($data["groups"]); $i++){
	if($data["groups"][$i]["id"] == $groupId){
		unset($data["groups"][$i]);
		$data["groups"] = array_values($data["groups"]);
	}
}


//Encode and return it
$jsonData = json_encode($data);
file_put_contents("data.json", $jsonData);
header('Content-Type: application/json');
echo $jsonData;





