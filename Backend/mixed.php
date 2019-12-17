<!DOCTYPE html PUBLIC>
<html xmlns>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>PHP - Ordner auslesen und anzeigen</title>
<style type="text/css">
div.galerie{
	padding: 3px;
	background-color:#ebebeb;
	border:1px solid #CCC;
	float:left;
	margin:10px 10px 0  0;
	font-family:Arial, Helvetica, sans-serif;
}
div.galerie:hover{
	border:1px solid #333;
}
div.galerie span{
	display:block;
	text-align:center;
	font-size:10px;
}
div.galerie a img{
		border:none;
}
div.file {
	padding:4px 4px 4px 30px;
}
div.file.even{
	background-color: #c9c9c9;
}
div.file a {
	text-decoration:none;
}
div.file:hover {
	background-color:#CCC;
}
</style>
<script type></script>
<script type="text/javascript">
$(document).ready(function() {
	$('.file:even').addClass('even');
});
</script>
</head>

<body>

	<?php

	$db = new mysqli('localhost','TestUser', 'root', 'drohnenProjekt');

	$erg = $db->query("Select * from flugdaten") or die($db->error);

	if($erg->num_rows){
	  '\n';
		echo "<p>Datenvorhanden: Anzahl ";
	  echo $erg->num_rows;
	}

	$datensatz = $erg->fetch_all(MYSQLI_ASSOC);

	foreach($datensatz as $zeile) {
	  echo '<br>';
	  echo '<br> ' . "Nr " . $zeile['Nr'] ;
	  echo '<br> ' . "Breitengrad " . $zeile['Breitengrad'] ;
	  echo '<br> ' . "Längengrad " . $zeile['Längengrad'] ;
		echo '<br> ' . "Hoehe " . $zeile['Hoehe'] ;
	  echo '<br> ' . "Kameraposition " . $zeile['Kameraposition'] ;
	}

		?>

<?php
$ordner = "mixed";
$alledateien = scandir($ordner);
$verzeichnispfad = 'C:\xampp\htdocs\IM\mixed\ ';       //Verzeichnispfad mit Leerzeihen, da ansonsten das \ als letztes steht und somit der restliche Code nicht klappt
$gekürzterVerzeichnispfad = rtrim($verzeichnispfad);   //den Verzeichnispfad kürzen also das Leerzeichen entfernen

foreach ($alledateien as $datei){

	$dateiinfo = pathinfo($ordner."/".$datei);

	$size = ceil(filesize($ordner."/".$datei)/1024);
if ($datei != "." && $datei != ".."  && $datei != "_notes") {

	//Datum und Uhrzeit ausgeben
		$pfad =$gekürzterVerzeichnispfad .$datei;
	  $timestamp =  filemtime($pfad);  //filemtime zeigt die letzte Änderung der Datei
	  $datum = date("d.m.Y",$timestamp);
	  $uhrzeit = date("H:i:s",$timestamp);
		$zeitpunkt = $datum . ' - '. $uhrzeit . ' Uhr';
	//	echo $zeitpunkt;

			//Bildtypen sammeln
			$bildtypen= array("jpg", "jpeg", "gif", "png");
			//Dateien nach Typ prüfen, in dem Fall nach Endungen für Bilder filtern
			if(in_array($dateiinfo['extension'],$bildtypen))
  			{
	?>
            <div class="galerie">
                <a href="<?php echo $dateiinfo['dirname']."/".$dateiinfo['basename'];?>">
                <img src="<?php echo $dateiinfo['dirname']."/".$dateiinfo['basename'];?>" width="140" alt="Vorschau" /></a>
                <span><?php echo $dateiinfo['filename']; ?> (<?php echo $size . 'kb)' .'</br>'. $zeitpunkt ; ?> </span>
            </div>
    		<?php
			// wenn keine Bildeindung dann normale Liste für Dateien ausgeben
			} else { ?>
            <div class="file">
            	<a href="
							<?php
							echo $dateiinfo['dirname']."/".$dateiinfo['basename'];
							?>">&raquo
							<?php
							echo $dateiinfo['filename'];
							?></a> (
							<?php
							 echo $dateiinfo['extension'] .' | '. $size . 'kb) ' . $zeitpunkt;
							 ?>
            </div>
            <?php } ?>
<?php
	};
 };
?>



</body>
</html>
