var jsonData;

function get6dofdata(){
  if(window.sdof){
    var ret = window.sdof.get6Dof();
    if(ret != ""){
      jsonData = JSON.parse(ret);
      postMessage(jsonData);
      // camera.position.set(0 - parseFloat(jsonDT.position.x), 0 - parseFloat(jsonDT.position.y) , 0 - parseFloat(jsonDT.position.z));
      // camera.quaternion.set(parseFloat(jsonDT.rotation.x), parseFloat(jsonDT.rotation.y), parseFloat(jsonDT.rotation.z), 0 - parseFloat(jsonDT.rotation.w));
    }
  }

  setTimeout("get6dofdata()", 1000/60);

  // setTimeout(function draw_canvas() {
  //     context.putImageData(imageData);
  //     setTimeout(draw_canvas, 1000/60);
  // },10);
}

get6dofdata();

onmessage = function(e){
  //TBD
}
