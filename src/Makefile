TARGET		= praca
BUILDDIR	= output
LATEX_CMD	= pdflatex -output-directory=${BUILDDIR} ${TARGET}.tex
BIBTEX_CMD	= bibtex ${TARGET}

build: ${BUILDDIR}/${TARGET}.pdf

run: build
	atril ${BUILDDIR}/$(TARGET).pdf &

# LaTeX je potreba spustit nekolikrat, aby spravne spocital odkazy
${BUILDDIR}/${TARGET}.pdf: *.tex *.bib */*.tex Makefile
	mkdir -p ${BUILDDIR}
	${LATEX_CMD}
	#${BIBTEX_CMD}
	#${LATEX_CMD}
	#${LATEX_CMD}

clean:
	rm -rf ${BUILDDIR}

