
const style = {};

const currentGeometry = () => {
    if (!style.width || !style.height) {
        const text = $('.xterm-helpers style').text();
        let arr = text.split('xterm-normal-char{width:');
        style.width = parseFloat(arr[1]);
        arr = text.split('div{height:');
        style.height = parseFloat(arr[1]);
    }
    const columns = parseInt(window.innerWidth / style.width, 10) - 1;
    const rows = parseInt(window.innerHeight / style.height, 10);
    return {columns, rows};
};

const action = (type, data) =>
    JSON.stringify({
        type,
        ...data,
    });

const resizeTerm = (term, ws) => {
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
        bellStyle: 'sound',
        // fontSize: 14,
        // lineHeight: 1.2,
        // letterSpacing: 0,
        // fontWeight: '400',
        // fontFamily: 'Consolas, "Courier New", monospace',
        theme: {
            foreground: "#ECECEC",
            background: "#000000",
            cursor: "help",
            lineHeight: 20
        }
        // rendererType: 'canvas',
        // scrollback: Number.MAX_SAFE_INTEGER
    });
    window.term = term;
    let searchAddon = new SearchAddon.SearchAddon();
    let searchbarAddon = new SearchBarAddon.SearchBarAddon({
        searchAddon
    });

    term.loadAddon(searchAddon);
    term.loadAddon(searchbarAddon);

    const fitAddon = new FitAddOn();
    this.term.loadAddon(fitAddon);

    // Open the terminal in #terminal-container
    term.open(document.getElementById('terminal'));

    // Make the terminal's size and geometry fit the size of #terminal-container
    fitAddon.fit();

    term.writeln('Welcome to cloud terminal!');

    searchbarAddon.show();

    term.onData(command => {
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
        let data = JSON.parse(e.data);
        switch (data.type) {
            case 'TERMINAL_PRINT':
                term.write(data.text);
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
        window.serverId = event.data.serverId;
        if (event.data.newTab) {
            window.open(location.origin + '/?serverId=' + serverId + "&name=" + event.data.name);
        } else {
            location.replace(location.origin + '/?serverId=' + serverId + "&name=" + event.data.name)
        }
    } catch (e) {
        console.log(e);
    }
}, false);
