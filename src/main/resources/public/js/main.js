const style = {};

const currentGeometry = () => {
    const text = $('.xterm-helpers style').text();
    let arr = text.split('xterm-normal-char{width:');
    style.width = parseFloat(arr[1]);
    arr = text.split('div{height:');
    style.height = parseFloat(arr[1]);
    const columns = parseInt(window.innerWidth / style.width, 10) - 1;
    const rows = parseInt(window.innerHeight / style.height, 10);
    return {columns, rows};
};

const action = (type, data) =>
    JSON.stringify({
        type,
        ...data,
    });

const resizeTerm = () => {
    const {columns, rows} = currentGeometry();
    if (columns !== term.geometry[0] || rows !== term.geometry[1]) {
        console.log(`resizing term to ${JSON.stringify({columns, rows})}`);
        term.resize(columns, rows);
        ws.send(
            action('TERMINAL_RESIZE', {
                columns,
                rows,
            })
        );
    }
};

$(() => {
    let sec = location.protocol.indexOf("https") > -1
    let name = location.search.substring(location.search.indexOf("&name=") + 6)
    if (name) document.title = name;
    let ws = new WebSocket(`${sec ? "wss" : "ws"}://${location.host}/terminal` + location.search);
    window.ws = ws; // for further use
    let term = new Terminal({
        cursorBlink: true,
    });
    window.term = term;

    term.on('data', command => {
        console.log(command);
        ws.send(
            action('TERMINAL_COMMAND', {
                command,
            })
        );
    });

    ws.onopen = () => {
        ws.send(action('TERMINAL_INIT'));
        ws.send(action('TERMINAL_READY'));
        term.open(document.getElementById('#terminal'), true);
        term.toggleFullscreen(true);
        $(window).resize(function () {
            resizeTerm(term, ws);
        });
    };

    ws.onmessage = e => {
        if (!term.resized) {
            resizeTerm(term, ws);
            term.resized = true;
        }
        if (typeof (e.data) == "string") { // text message
            let data = JSON.parse(e.data);
            switch (data.type) {
                case 'TERMINAL_PRINT':
                    term.write(data.text);
            }
        } else { // file download
            let reader = new FileReader();
            reader.onload = function (e) {
                if (e.target.readyState == FileReader.DONE) {
                    let url = e.target.result;
                    let img = document.getElementById("imgDiv");
                    img.innerHTML = "<img src = " + url + " />";
                }
            }
            reader.readAsDataURL(e.data);
        }
    };

    ws.onerror = e => {
        console.log(e);
    };

    ws.onclose = e => {
        console.log(e);
        term.destroy();
    };
});

window.addEventListener('message', function (event) {
    try {
        window.sessionId = event.data.sessionId;
        if (event.data.newTab) {
            window.open(location.origin + '/?sessionId=' + sessionId + "&name=" + event.data.name);
        } else {
            location.replace(location.origin + '/?sessionId=' + sessionId + "&name=" + event.data.name)
        }
    } catch (e) {
        console.log(e);
    }
}, false);
