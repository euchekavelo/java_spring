function getData(address, data, cb, cbErr) {
    $.ajax({
        url: address,
        type: 'POST',
        dataType: 'json',
        data: JSON.stringify(data),
        headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json'
        },
        complete: function (result) {
            if (result.status === 200) {
                cb(result.responseJSON);
            } else {
                cbErr(result.responseJSON);
            }
        }
    });
}

$(document).ready(function () {
    $('.save-btn').on('click', function (e) {
        e.preventDefault();

        let form = $(".form-group");
        getData('/profile', {
            name: form.find("#name").val(),
            mail: form.find("#mail").val(),
            phone: form.find("#phone").val(),
            password: form.find("#password").val(),
            passwordReply: form.find("#passwordReply").val(),
        }, function (result) {
            if (result.result) {
                window.location.reload();
            } else {
                $('#btn-profile').after('<div class="Profile-error" style="color:red">' + result.error + '</div>');
            }
        })
    });

    $('.confirm_email').on('click', function (e) {
        e.preventDefault();

        let form = $(".form-group");
        getData('/mailConfirmation', {
            email: form.find("#mail").val()
        }, function (result) {
            if (result.result) {
                let btnSegment = $('#btn_confirm_email');
                btnSegment.empty();
                btnSegment.append('<input class="input_code_mail" style="margin-right: 20px; margin-bottom: 20px; width: 100px" type="number" onInput="this.value = this.value.slice(0, 6)" />');
                btnSegment.append('<button class="check_code_email" type="button">Проверить код</button>');
            }
        })
    });

    $('#btn_confirm_email').on('click', '.check_code_email', function (e) {
        e.preventDefault();

        getData('/mailCodeVerification', {
            code: $(".input_code_mail").val()
        }, function (result) {
            let btnSegment = $('#btn_confirm_email');
            if (result.result) {
                btnSegment.empty();
                btnSegment.append('<div class="Profile-success">Код, полученный по действующей почте, успешно подтвержден</div>');
            } else {
                btnSegment.empty();
                btnSegment.append('<div class="Profile-error" style="color:red">' + result.error + '</div>');
            }
        })
    });

    $('.confirm_phone').on('click', function (e) {
        e.preventDefault();

        let form = $(".form-group");
        getData('/phoneConfirmation', {
            phone: form.find("#phone").val()
        }, function (result) {
            if (result.result) {
                let btnSegment = $('#btn_confirm_phone');
                btnSegment.empty();
                btnSegment.append('<input class="input_code_phone" style="margin-right: 20px; margin-bottom: 20px; width: 100px" type="number" onInput="this.value = this.value.slice(0, 6)" />');
                btnSegment.append('<button class="check_code_phone" type="button">Проверить код</button>');
            }
        })
    });

    $('#btn_confirm_phone').on('click', '.check_code_phone', function (e) {
        e.preventDefault();

        getData('/phoneCodeVerification', {
            code: $(".input_code_phone").val()
        }, function (result) {
            let btnSegment = $('#btn_confirm_phone');
            if (result.result) {
                btnSegment.empty();
                btnSegment.append('<div class="Profile-success">Код, полученный по действующему номеру телефона, успешно подтвержден</div>');
            } else {
                btnSegment.empty();
                btnSegment.append('<div class="Profile-error" style="color:red">' + result.error + '</div>');
            }
        })
    });
})
