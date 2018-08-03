$(function () {
    var $canvas = $("#img"),
        context = $canvas.get(0).getContext('2d');

        $("#dragModeMove").on("click", function () {
            //var cropperImage = $canvas.cropper('getCroppedCanvas').toDataURL('image/jpg');
            $('#canvas').cropper('setDragMode', 'move');
        });
    
        $("#dragModeCrop").on("click", function () {
            //var cropperImage = $canvas.cropper('getCroppedCanvas').toDataURL('image/jpg');
            $('#canvas').cropper('setDragMode','crop');
        });
        var x = 1;
        $("#scaleX").on("click", function () {
            //var cropperImage = $canvas.cropper('getCroppedCanvas').toDataURL('image/jpg');
            
            $('#canvas').cropper('scaleX', x*=-1);
    
        });
        var y = 1;
        $("#scaleY").on("click", function () {
            //var cropperImage = $canvas.cropper('getCroppedCanvas').toDataURL('image/jpg');
            $('#canvas').cropper('scaleY', y*=-1);
        });
    
    
        $("#rotateLeft").on("click", function () {
            //var cropperImage = $canvas.cropper('getCroppedCanvas').toDataURL('image/jpg');
            $('#canvas').cropper('rotate', 45);
        });
    
        $("#rotateRight").on("click", function () {
            //var cropperImage = $canvas.cropper('getCroppedCanvas').toDataURL('image/jpg');
            $('#canvas').cropper('rotate', -45);
        });
    
        $("#moveLeft").on("click", function () {
            //var cropperImage = $canvas.cropper('getCroppedCanvas').toDataURL('image/jpg');
            $('#canvas').cropper('move', -50, 0);
        });
    
        $("#moveRight").on("click", function () {
            //var cropperImage = $canvas.cropper('getCroppedCanvas').toDataURL('image/jpg');
            $('#canvas').cropper('move', 50, 0);
        });
    
    
        $("#moveUp").on("click", function () {
            //var cropperImage = $canvas.cropper('getCroppedCanvas').toDataURL('image/jpg');
            $('#canvas').cropper('move', 0, -50);
        });
    
        $("#moveDown").on("click", function () {
            //var cropperImage = $canvas.cropper('getCroppedCanvas').toDataURL('image/jpg');
            $('#canvas').cropper('move', 0, 50);
        });

        $("#zoomPlus").on("click", function () {
            //var cropperImage = $canvas.cropper('getCroppedCanvas').toDataURL('image/jpg');
            $('#img').cropper('zoom', 0.1);
        });
    
        $("#zoomMinus").on("click", function () {
            //var cropperImage = $canvas.cropper('getCroppedCanvas').toDataURL('image/jpg');
            $('#img').cropper('zoom', -0.1);
        });

    $('#loadImg').on('change', function () {
        if (this.files && this.files[0]) {
            if (this.files[0].type.match(/^image\//)) {
                var reader = new FileReader();

                $("#exampleModalCenter").modal();

                reader.onload = function (e) {
                    var img = new Image();
                    img.onload = function () {
                        context.canvas.width = img.width;
                        context.canvas.height = img.height;
                        context.drawImage(img, 0, 0);

                        var cropper = $canvas.cropper('destroy').cropper({
                            aspectRatio: 1 / 1,
                            dragMode: 'move',
                            preview: '.preview',
                            minCropBoxHeight: 100,
                            minCropBoxWidth: 100,
                        });
                    }
                    img.src = e.target.result;
                };
                $("#crop").click(function () {
                    var croppedImage = $canvas.cropper('getCroppedCanvas', {width: 240, height: 240}).toDataURL('image/jpg');
                    $('#result').html($('<img>').attr('src', croppedImage));
                    console.log(croppedImage);
                    //Зображення обрізане записуємо у скрите поле на формі
                    $('#ImageBase64').attr("value", croppedImage);
                    $(".containerCrop").hide();
                    $(".navbar").show();
                });

                reader.readAsDataURL(this.files[0]);
                this.value = '';
            }
            else {
                alert("Invalid file type");
            }
        }
        else {
            alert("Please select a file.");
        }

    });
});