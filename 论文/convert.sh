#!/usr/bin/env bash
# ===================================================================
# FundTogether 毕业设计论文格式转换脚本
# 功能：基于 Pandoc 将 thesis.md 转换为符合太原理工大学软件工程专业
#       毕业设计（论文）写作规范的 .docx 与 .pdf（可选）文档
# 依赖：pandoc >= 3.0 （推荐安装路径 /opt/homebrew/bin/pandoc）
# 使用：bash convert.sh
# ===================================================================

set -euo pipefail

# ---------- 路径与工具 ----------
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "${SCRIPT_DIR}"

INPUT="thesis.md"
OUTPUT_DOCX="在线众筹网站的设计与实现-郭家旗.docx"
OUTPUT_PDF="在线众筹网站的设计与实现-郭家旗.pdf"

PANDOC_BIN="${PANDOC_BIN:-$(command -v pandoc || echo /opt/homebrew/bin/pandoc)}"
if [[ ! -x "${PANDOC_BIN}" ]]; then
  echo "[ERROR] 未找到 pandoc，请先执行 'brew install pandoc'" >&2
  exit 1
fi

echo "[INFO] 使用 Pandoc: ${PANDOC_BIN}"
"${PANDOC_BIN}" --version | head -n 1

# ---------- 生成 Word (.docx) ----------
echo "[INFO] 开始转换 Markdown -> DOCX ..."
"${PANDOC_BIN}" "${INPUT}" \
  -f markdown+smart+raw_tex \
  -t docx \
  -o "${OUTPUT_DOCX}" \
  --toc \
  --toc-depth=3 \
  --number-sections \
  --resource-path=.

echo "[OK]  已生成: ${OUTPUT_DOCX}"

# ---------- 可选：生成 PDF ----------
if command -v xelatex >/dev/null 2>&1; then
  echo "[INFO] 检测到 xelatex，开始转换 Markdown -> PDF ..."
  "${PANDOC_BIN}" "${INPUT}" \
    -f markdown+smart+raw_tex \
    -o "${OUTPUT_PDF}" \
    --pdf-engine=xelatex \
    -V CJKmainfont="Songti SC" \
    -V mainfont="Times New Roman" \
    -V geometry:"top=2.54cm,bottom=2.54cm,left=3.18cm,right=3.18cm" \
    -V linestretch=1.5 \
    -V fontsize=12pt \
    --toc \
    --toc-depth=3 \
    --number-sections || {
      echo "[WARN] PDF 生成失败（通常因字体或 xelatex 配置），仅保留 DOCX"
    }
fi

# ---------- 字符数统计 ----------
echo "[INFO] 论文字符数统计："
wc -m "${INPUT}" | awk '{printf "  thesis.md: %s 字符\n", $1}'

echo "[DONE] 转换完成。"
