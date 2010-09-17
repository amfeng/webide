$(document).ready(function() {
   bindCSS();
});

function bindCSS(){
  var cssLink = document.createElement("link");
  cssLink.href = "style.css";
  cssLink.rel = "stylesheet";
  cssLink.type = "text/css";
  window.frames["frame_editorpane"].document.body.appendChild(cssLink);
}

function highlightLine(line, error){
  console.log(error)
  window.frames["frame_editorpane"].children[0].children["line_" + line].innerHTML = "<img src='images/exclamation.png' title='" + error + "' />" + line;
}