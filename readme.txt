This folder contains files in a GitHub repository used for the ACH Microgrant DHQ Citation Network project. It is organized as follows:

data
	counts: initial scrape of DHQ data to make sure a) citations were labelled in such a way as to be scrapable and b) determine the rough number of citations in a HQ citation datatset
	other
		articles-30.xlsx, articles-mallet.txt are files produced by the document similarity topic modeling process
		docdoc.csv and doctopic.csv are individual sheets from the spreadsheet produced by the topic modeling process, organized for importation into Gephi
		citations.txt is a scrape of all th citations in the DHQ articles
		links.csv is data on citation links from each of the 100 DHQ articles to other articles (DHQ or external), plus a weight column signifying how mnay times the citation in question is mentioned in the article
	text: the 110 DHQ articles rendered as XML files
gephi files
	.gephi files used for visualizations 1-4
log
	log of errors found while getting data ready to be visualized (e.g. works mentioned in bibliographies but not cited in main text)
scripts
	CountCitations.scala, the script Travis used to count citations within the DHQ dataset