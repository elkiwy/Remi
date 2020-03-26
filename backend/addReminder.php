<?php

//Get params
$groupName = $_GET["group"];
$reminderText = $_GET["text"];

//Get data
$jsonData = file_get_contents("data.json");
$data = json_decode($jsonData, true);

//Find a usableId
$maxId = 1;
foreach($data["groups"] as &$group){
	foreach($group["reminders"] as &$reminder){
		if($maxId<$reminder["id"]){
			$maxId = $reminder["id"];
		}		
	}
}
$newId = $maxId + 1;

//Search for the right group
foreach($data["groups"] as &$group){
	if($group["name"] == $groupName){
		//Add the reminder
		array_push($group["reminders"], array(
			"id" => $newId,
			"text" => $reminderText,
			"added" => date("Y-m-d-H-i-s"),
			"status" => 0,
			"scheduled" => "",
			"priority" => 0
		));
	}
}

//Encode and return it
$jsonData = json_encode($data);
file_put_contents("data.json", $jsonData);
header('Content-Type: application/json');
echo $jsonData;





