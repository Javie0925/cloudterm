!(function(e, t) {
  'object' == typeof exports && 'object' == typeof module
    ? (module.exports = t())
    : 'function' == typeof define && define.amd
    ? define([], t)
    : 'object' == typeof exports
    ? (exports.SearchAddon = t())
    : (e.SearchAddon = t());
})(window, function() {
  return (function(e) {
    var t = {};
    function i(r) {
      if (t[r]) return t[r].exports;
      var n = (t[r] = { i: r, l: !1, exports: {} });
      return e[r].call(n.exports, n, n.exports, i), (n.l = !0), n.exports;
    }
    return (
      (i.m = e),
      (i.c = t),
      (i.d = function(e, t, r) {
        i.o(e, t) || Object.defineProperty(e, t, { enumerable: !0, get: r });
      }),
      (i.r = function(e) {
        'undefined' != typeof Symbol &&
          Symbol.toStringTag &&
          Object.defineProperty(e, Symbol.toStringTag, { value: 'Module' }),
          Object.defineProperty(e, '__esModule', { value: !0 });
      }),
      (i.t = function(e, t) {
        if ((1 & t && (e = i(e)), 8 & t)) return e;
        if (4 & t && 'object' == typeof e && e && e.__esModule) return e;
        var r = Object.create(null);
        if (
          (i.r(r),
          Object.defineProperty(r, 'default', { enumerable: !0, value: e }),
          2 & t && 'string' != typeof e)
        )
          for (var n in e)
            i.d(
              r,
              n,
              function(t) {
                return e[t];
              }.bind(null, n)
            );
        return r;
      }),
      (i.n = function(e) {
        var t =
          e && e.__esModule
            ? function() {
                return e.default;
              }
            : function() {
                return e;
              };
        return i.d(t, 'a', t), t;
      }),
      (i.o = function(e, t) {
        return Object.prototype.hasOwnProperty.call(e, t);
      }),
      (i.p = ''),
      i((i.s = 0))
    );
  })([
    function(e, t, i) {
      'use strict';
      Object.defineProperty(t, '__esModule', { value: !0 });
      var r = (function() {
        function e() {
          this._linesCacheTimeoutId = 0;
        }
        return (
          (e.prototype.activate = function(e) {
            this._terminal = e;
          }),
          (e.prototype.dispose = function() {}),
          (e.prototype.findNext = function(e, t) {
            if (!this._terminal) throw new Error('Cannot use addon until it has been loaded');
            if (!e || 0 === e.length) return this._terminal.clearSelection(), !1;
            var i,
              r = 0,
              n = 0;
            if (this._terminal.hasSelection()) {
              var o = !!t && t.incremental;
              (i = this._terminal.getSelectionPosition()),
                (n = o ? i.startRow : i.endRow),
                (r = o ? i.startColumn : i.endColumn);
            }
            this._initLinesCache();
            var s = { startRow: n, startCol: r },
              a = this._findInLine(e, s, t);
            if (!a)
              for (
                var l = n + 1;
                l < this._terminal.buffer.baseY + this._terminal.rows &&
                ((s.startRow = l), (s.startCol = 0), !(a = this._findInLine(e, s, t)));
                l++
              );
            if (!a && 0 !== n)
              for (
                l = 0;
                l < n && ((s.startRow = l), (s.startCol = 0), !(a = this._findInLine(e, s, t)));
                l++
              );
            return !(a || !i) || this._selectResult(a);
          }),
          (e.prototype.findPrevious = function(e, t) {
            if (!this._terminal) throw new Error('Cannot use addon until it has been loaded');
            if (!e || 0 === e.length) return this._terminal.clearSelection(), !1;
            var i,
              r,
              n = this._terminal.buffer.baseY + this._terminal.rows,
              o = this._terminal.cols,
              s = !!t && t.incremental;
            this._terminal.hasSelection() &&
              ((n = (r = this._terminal.getSelectionPosition()).startRow), (o = r.startColumn)),
              this._initLinesCache();
            var a = { startRow: n, startCol: o };
            if (
              ((s && (i = this._findInLine(e, a, t, !1)) && i.row === n && i.col === o) ||
                (i = this._findInLine(e, a, t, !0)),
              !i)
            ) {
              a.startCol = Math.max(a.startCol, this._terminal.cols);
              for (
                var l = n - 1;
                l >= 0 && ((a.startRow = l), !(i = this._findInLine(e, a, t, !0)));
                l--
              );
            }
            if (!i && n !== this._terminal.buffer.baseY + this._terminal.rows)
              for (
                l = this._terminal.buffer.baseY + this._terminal.rows;
                l > n && ((a.startRow = l), !(i = this._findInLine(e, a, t, !0)));
                l--
              );
            return !(i || !r) || this._selectResult(i);
          }),
          (e.prototype._initLinesCache = function() {
            var e = this,
              t = this._terminal;
            this._linesCache ||
              ((this._linesCache = new Array(t.buffer.length)),
              (this._cursorMoveListener = t.onCursorMove(function() {
                return e._destroyLinesCache();
              })),
              (this._resizeListener = t.onResize(function() {
                return e._destroyLinesCache();
              }))),
              window.clearTimeout(this._linesCacheTimeoutId),
              (this._linesCacheTimeoutId = window.setTimeout(function() {
                return e._destroyLinesCache();
              }, 15e3));
          }),
          (e.prototype._destroyLinesCache = function() {
            (this._linesCache = void 0),
              this._cursorMoveListener &&
                (this._cursorMoveListener.dispose(), (this._cursorMoveListener = void 0)),
              this._resizeListener &&
                (this._resizeListener.dispose(), (this._resizeListener = void 0)),
              this._linesCacheTimeoutId &&
                (window.clearTimeout(this._linesCacheTimeoutId), (this._linesCacheTimeoutId = 0));
          }),
          (e.prototype._isWholeWord = function(e, t, i) {
            return !(
              (0 !== e && -1 === ' ~!@#$%^&*()+`-=[]{}|;:"\',./<>?'.indexOf(t[e - 1])) ||
              (e + i.length !== t.length &&
                -1 === ' ~!@#$%^&*()+`-=[]{}|;:"\',./<>?'.indexOf(t[e + i.length]))
            );
          }),
          (e.prototype._findInLine = function(e, t, i, r) {
            void 0 === i && (i = {}), void 0 === r && (r = !1);
            var n = this._terminal,
              o = t.startRow,
              s = t.startCol,
              a = n.buffer.getLine(o);
            if (a && a.isWrapped)
              return r
                ? void (t.startCol += n.cols)
                : (t.startRow--, (t.startCol += n.cols), this._findInLine(e, t, i));
            var l = this._linesCache ? this._linesCache[o] : void 0;
            void 0 === l &&
              ((l = this._translateBufferLineToStringWithWrap(o, !0)),
              this._linesCache && (this._linesCache[o] = l));
            var f = i.caseSensitive ? e : e.toLowerCase(),
              h = i.caseSensitive ? l : l.toLowerCase(),
              u = -1;
            if (i.regex) {
              var c = RegExp(f, 'g'),
                d = void 0;
              if (r)
                for (; (d = c.exec(h.slice(0, s))); )
                  (u = c.lastIndex - d[0].length), (e = d[0]), (c.lastIndex -= e.length - 1);
              else
                (d = c.exec(h.slice(s))) &&
                  d[0].length > 0 &&
                  ((u = s + (c.lastIndex - d[0].length)), (e = d[0]));
            } else
              r ? s - f.length >= 0 && (u = h.lastIndexOf(f, s - f.length)) : (u = h.indexOf(f, s));
            if (u >= 0) {
              if (
                (u >= n.cols && ((o += Math.floor(u / n.cols)), (u %= n.cols)),
                i.wholeWord && !this._isWholeWord(u, h, e))
              )
                return;
              var _ = n.buffer.getLine(o);
              if (_)
                for (var p = 0; p < u; p++) {
                  var v = _.getCell(p);
                  if (!v) break;
                  var m = v.char;
                  m && m.length > 1 && (u -= m.length - 1), 0 === v.width && u++;
                }
              return { term: e, col: u, row: o };
            }
          }),
          (e.prototype._translateBufferLineToStringWithWrap = function(e, t) {
            var i,
              r = this._terminal,
              n = '';
            do {
              var o = r.buffer.getLine(e + 1);
              i = !!o && o.isWrapped;
              var s = r.buffer.getLine(e);
              if (!s) break;
              (n += s.translateToString(!i && t).substring(0, r.cols)), e++;
            } while (i);
            return n;
          }),
          (e.prototype._selectResult = function(e) {
            var t = this._terminal;
            if (!e) return t.clearSelection(), !1;
            if (
              (t.select(e.col, e.row, e.term.length),
              e.row >= t.buffer.viewportY + t.rows || e.row < t.buffer.viewportY)
            ) {
              var i = e.row - t.buffer.viewportY;
              (i -= Math.floor(t.rows / 2)), t.scrollLines(i);
            }
            return !0;
          }),
          e
        );
      })();
      t.SearchAddon = r;
    }
  ]);
});
//# sourceMappingURL=xterm-addon-search.js.map
