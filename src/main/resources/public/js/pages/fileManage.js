const templateText =
    `
        <div class="layui-colla-item" id="{{ formatPath(d.absolutePath) }}">
            <h2 class="layui-colla-title" data-json='{{ JSON.stringify(d) }}' data-loaded="false" onclick='expendDir(this)'>
                {{# if(d.directory){ }}
                    <svg style="width: inherit;height: inherit;" t="1669108074474" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="6555" width="200" height="200">
                        <path d="M958.547801 316.377624l0-8.963139c0-40.48711-32.827663-73.319889-73.315796-73.319889L511.504208 234.094596c-72.308863-96.943936-62.007239-114.116037-114.692159-114.116037l-262.07895 0c-40.483017 0-73.314773 32.831756-73.314773 73.314773l0 123.084293L958.547801 316.377624z" p-id="6556"></path>
                        <path d="M61.41935 381.869221l0 431.233512c0 40.481993 32.831756 73.315796 73.314773 73.315796l750.498906 0c40.48711 0 73.315796-32.833803 73.315796-73.315796L958.548824 381.869221 61.41935 381.869221z" p-id="6557"></path>
                    </svg>
                {{# }else{ }}
                    <svg style="width: inherit;height: inherit;" t="1669108367445" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="6696" width="200" height="200"><path d="M864 1024 160 1024c-17.696 0-32-14.304-32-32L128 320l32 0 0 0 256 0c17.696 0 32-14.336 32-32L448 128 448 32 448 0l416 0c17.696 0 32 14.336 32 32l0 960C896 1009.696 881.696 1024 864 1024zM384 0l0 128 0 128L128 256 384 0z" p-id="6697"></path></svg>
                {{# } }}
                {{ d.name?d.name:d.absolutePath }}
            </h2>
            <div class="layui-colla-content">
                {{#  if(d.directory){ }}
                    <div class="layui-collapse" id="{{ formatPath(d.absolutePath,'collapse') }}">
                        <div style="display: flex;justify-content: center" id="loading">
                            <img src="js/layui/css/modules/layer/default/loading-2.gif">
                        </div>
                    </div>
                {{# }else{ }}
                    <div class="row justify-content-between align-content-center ms-3">
                        <div class="col" style="margin-left:3%">{{ d.name }}</div>
                        <div class="col-3">{{ new Date(d.lastModified).toJSON().replaceAll('T',' ').split('.')[0] }}</div>
                        <div class="col-2">{{ formatSize(d.size) }}</div>
                        <div class="col-2">
                          <button type="button" class="layui-btn layui-btn-sm" data-path="{{ d.absolutePath }}" data-filename="{{ d.name }}" onclick="download(this)">Download</button>
                        </div>
                    </div>
                {{# } }} 
            </div>
        </div>
    `;

layui.use(['table'], function () {
    let table = layui.table, $ = layui.$, layer = layui.layer;

    layer.config({
        extend: 'console-skin/style.css', //load customized skin style
        skin: 'layui-skin-console'
    });

    $.ajax("/file/listRoots" + parent.location.search, {
        success: function (res) {
            if (res.code == 0) {
                let roots = eval(res.data);
                appendItems(roots, "#collapse-container")
            }
        }
    })
});
let $ = layui.$;

function appendItems(roots, target) {
    $(target).html('')
    for (let root of roots) {
        layui.laytpl(templateText).render(root, function (res) {
            $(target).append(res)
        });
    }
    layui.element.init()
}

function formatSize(size) {
    if (size <= 1024) {
        return Math.ceil(size * 100) / 100 + 'B'
    } else if (size <= 1024 * 1024) { // < 1MB
        return Math.ceil(size / 1024 * 100) / 100 + 'KB'
    } else if (size <= 1024 * 1024 * 1024) { // <1GB
        return Math.ceil(size / 1024 / 1024 * 100) / 100 + 'MB'
    } else {
        return Math.ceil((size / 1024 / 1024 / 1024) * 100) / 100 + 'GB'
    }
}

function formatPath(path, suffix) {
    return path
        .replaceAll('\\', '')
        .replaceAll(':', '')
        .replaceAll('.', '')
        .replaceAll(' ', '')
        .replaceAll('$', '')
        .replaceAll('/', '')
        .replaceAll('~', '')
        .concat(suffix ? ("_" + suffix) : '')
}

function expendDir(that) {
    if ($(that).data("loaded")) return
    let data = $(that).data("json")
    $.ajax("/file/listFilesByPath" + parent.location.search, {
        data: {
            path: data.absolutePath
        },
        success: function (res) {
            if (res.code == 0) {
                appendItems(eval(res.data), "#" + formatPath(data.absolutePath, 'collapse'))
                $(that).data("loaded", true)
            }
        }
    })
}

function download(that) {
    let path = $(that).data("path")
    let filename = $(that).data("filename")
    console.log(path)
    if (!path) return
    let search = '';
    if (!parent.location.search) {
        search = "?path=" + encodeURIComponent(path) + "&filename=" + encodeURIComponent(filename)
    } else {
        search = parent.location.search + "&path=" + encodeURIComponent(path) + "&filename=" + encodeURIComponent(filename)
    }
    window.open("/file/download" + search)
}
