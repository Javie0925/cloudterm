const templateText =
    `
        <div class="layui-colla-item" id="{{ formatPath(d.absolutePath) }}">
            <h2 class="layui-colla-title" data-json='{{ JSON.stringify(d) }}' data-dest="#{{ formatPath(d.absolutePath, 'collapse') }}" data-loaded="false" onclick='expendDir(this)'>
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
                        <div class="col" style="margin-left:3%;overflow: hidden;white-space: nowrap;text-overflow: ellipsis;">{{ d.name }}</div>
                        <div class="col-3" style="overflow: hidden;white-space: nowrap;text-overflow: ellipsis;">{{ new Date(d.lastModified).toJSON().replaceAll('T',' ').split('.')[0] }}</div>
                        <div class="col-2">{{ formatSize(d.size) }}</div>
                        <div class="col-2">
                          <button type="button" class="layui-btn layui-btn-sm" style="margin-left:3%;overflow: hidden;white-space: nowrap;text-overflow: ellipsis;" data-path="{{ d.absolutePath }}" data-filename="{{ d.name }}" onclick="download(this)">Download</button>
                        </div>
                    </div>
                {{# } }} 
            </div>
        </div>
    `;

const fileDestTemplateText =
    `
        <div class="layui-colla-item" id="{{ formatPath(d.absolutePath) }}">
            <h2 class="layui-colla-title" data-json='{{ JSON.stringify(d) }}' data-dest="#{{ formatPath(d.absolutePath, 'fileDest-collapse') }}" data-loaded="false" onclick="expendDir(this,true)">
                <svg style="width: inherit;height: inherit;" t="1669108074474" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="6555" width="200" height="200">
                    <path d="M958.547801 316.377624l0-8.963139c0-40.48711-32.827663-73.319889-73.315796-73.319889L511.504208 234.094596c-72.308863-96.943936-62.007239-114.116037-114.692159-114.116037l-262.07895 0c-40.483017 0-73.314773 32.831756-73.314773 73.314773l0 123.084293L958.547801 316.377624z" p-id="6556"></path>
                    <path d="M61.41935 381.869221l0 431.233512c0 40.481993 32.831756 73.315796 73.314773 73.315796l750.498906 0c40.48711 0 73.315796-32.833803 73.315796-73.315796L958.548824 381.869221 61.41935 381.869221z" p-id="6557"></path>
                </svg>
                {{ d.name?d.name:d.absolutePath }}
                <button class="layui-btn layui-btn-sm" data-dest="{{ d.absolutePath }}" style="position: absolute;right: 0;top: 25%;background-color: var(--console-bg);border-color: var(--console-color);" onclick="selectDest(this)">select</button>
            </h2>
            <div class="layui-colla-content">
                <div class="layui-collapse" id="{{ formatPath(d.absolutePath,'fileDest-collapse') }}">
                    <div style="display: flex;justify-content: center" id="loading">
                        <img src="js/layui/css/modules/layer/default/loading-2.gif">
                    </div>
                </div>
            </div>
        </div>
    `;
let uploadModal, fileDestModal;
layui.use(['table', 'util', 'upload', 'layer'], function () {
    let table = layui.table, $ = layui.$, layer = layui.layer, util = layui.util, upload = layui.upload;
    layer.config({
        extend: 'console-skin/style.css', //load customized skin style
        skin: 'layui-skin-console'
    });

    //bootstrap modals
    uploadModal = new bootstrap.Modal(document.getElementById('uploadModal'), {});
    fileDestModal = new bootstrap.Modal(document.getElementById('fileDestModal'), {});

    //init roots
    $.ajax("/file/listRoots" + parent.location.search, {
        success: function (res) {
            if (res.code == 0) {
                let roots = eval(res.data);
                appendItems(roots, "#collapse-container")
            }
        }
    })

    // fixed tool bar
    util.fixbar({
        bar1: {
            title: "upload",
            icon: '&#xe67c;',
            type: "upload"
        }
        , css: {right: 10, bottom: 0}
        , bgcolor: '#c15050'
        , click: function (type) {
            if (type === 'upload') {
                uploadModal.show();
            }
        }
    });
    //upload file
    upload.render({
        elem: '#uploadDrag',
        auto: false,
        url: '/file/upload' + parent.location.search, //destination url
        data: {
            dest: function () {
                return $('#destInput').val();
            }
        },
        accept: 'file',
        bindAction: '#uploadSubmit',
        before: function () {
            layer.load();
        },
        done: function (data) {
            layer.closeAll('loading');
            if (data.code == 0) {
                layer.msg("(*^_^*)Upload Succeeded.");
            } else {
                layer.msg("Upload Failed, " + data.error, {
                    anim: 6
                });
            }
        },
        error: function () {
            layer.closeAll('loading');
            layer.msg("Upload Failed!", {
                anim: 6
            });
        }
    });
    // listen to file dest input box click
    $("#destInput").click(function (event) {
        initUploadDestCollapse(fileDestModal);
    })
});
let $ = layui.$;

function appendItems(roots, target, tplText) {
    $(target).html('')
    if (!roots || roots.length == 0) {
        $(target).append(
            `
                <div class="layui-collapse"">
                    <div style="display: flex;justify-content: center" id="loading">
                        <i class="layui-icon layui-icon-face-surprised" style="font-size: 18px;font-weight: bolder">Empty!</i>
                    </div>
                </div>
            `
        )
    } else {
        for (let root of roots) {
            layui.laytpl(tplText ? tplText : templateText).render(root, function (res) {
                $(target).append(res)
            });
        }
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
    let res = path.replaceAll(/[^0-9a-zA-Z]/g, '');
    if (!res) res = 'root-'
    return res.concat(hashCode(path)).concat(suffix ? ("-" + suffix) : '')
}

hashCode = function (str) {
    // 1315423911=b'1001110011001111100011010100111'
    let hash = 1315423911, i, ch;
    for (i = str.length - 1; i >= 0; i--) {
        ch = str.charCodeAt(i);
        hash ^= ((hash << 5) + ch + (hash >> 2));
    }

    return (hash & 0x7FFFFFFF);
}

function expendDir(that, toFileDest) {
    if ($(that).data("loaded")) return
    let data = $(that).data("json")
    let dest = $(that).data("dest")
    $.ajax("/file/listFilesByPath" + parent.location.search, {
        data: {
            path: data.absolutePath,
            dirOnly: toFileDest ? true : false
        },
        success: function (res) {
            if (res.code == 0) {
                appendItems(eval(res.data), dest, toFileDest ? fileDestTemplateText : '')
                $(that).data("loaded", true)
            } else {
                layer.msg(res.error)
                appendItems(eval(res.data), dest, toFileDest ? fileDestTemplateText : '')
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

function initUploadDestCollapse(fileDestModal) {
    $.ajax("/file/listRoots" + parent.location.search, {
        data: {
            dirOnly: true
        },
        success: function (res) {
            if (res.code == 0) {
                let roots = eval(res.data);
                appendItems(roots, "#file-dest-collapse-container", fileDestTemplateText)
                fileDestModal.show()
            }
        }
    })

}

function selectDest(that) {
    let dest = $(that).data("dest")
    $("#destInput").val(dest)
    fileDestModal.hide();
    console.log(dest)
}
