import React from "react";

interface DataTableProps {
    rows: React.JSX.Element[],
    rowTexts: string[]
    pageSize: number,
    children: React.JSX.Element
    hr?: React.JSX.Element
}

export const DataTable = ({rows, rowTexts, pageSize, children, hr}: DataTableProps) => {
    const [filter, setFilter] = React.useState("")
    const [pageSelection, setPageSelection] = React.useState(1)

    const filteredRows = rows
        .map((element, index) => {
            if (rowTexts[index].toLowerCase().includes(filter.toLowerCase())) {
                return element
            }
            return null
        })
        .filter((element) => element !== null) as React.JSX.Element[]

    const {pagedRows, page, totalPages} = determinePagingInfo(filteredRows, pageSelection, pageSize)


    return <>
        <div className="row text">
            <div className="col-lg-3">
                <input type="text" className="form-control" style={{height: "18px", paddingLeft: "6px"}} placeholder="filter"
                       value={filter} onChange={(event) => {
                    setFilter(event.target.value)
                }}/>
            </div>
            <div className="col-lg-3">
            </div>
            <PageButtons totalPages={totalPages} page={page} rowCount={rows.length} pageSize={pageSize} setPageSelection={setPageSelection}/>
        </div>
        <br/>
        {children}
        {pagedRows.map((row, index) => {
            return (<span key={index}>
                    {hr}
                    {row}
                </span>
            )
        })
        }
        {hr}
    </>
}


const determinePagingInfo = (rows: React.JSX.Element[], pageSelection: number, pageSize: number) => {
    const rowCount = rows.length

    if (rowCount === 0) {
        return {
            totalPages: 1,
            page: 1,
            pagedRows: []
        }
    }

    const totalPages = Math.ceil(rowCount / pageSize)
    const page = Math.max(1, Math.min(pageSelection, totalPages))

    const start = (page - 1) * pageSize
    const length = Math.min(start + pageSize, rowCount) - start

    const pagedRows = rows.splice(start, length)

    return {
        totalPages,
        page: page,
        pagedRows
    }

}

interface PageButtonsProps {
    totalPages: number,
    page: number,
    rowCount: number,
    pageSize: number,
    setPageSelection: (page: number) => void
}

const PageButtons = ({totalPages, page, rowCount, pageSize, setPageSelection}: PageButtonsProps) => {
    if (totalPages === 1) {
        return <></>
    }

    const start = rowCount > 0 ? 1 + (page - 1) * pageSize : 0
    const end = Math.min(start + pageSize - 1, (rowCount))

    return <>
        <div className="col-lg-3">
            {`${start}-${end} of ${rowCount}`}
        </div>
        <div className="col-lg-3">
            <PageButton text="<<" currentPage={page} targetPage={1} setPageSelection={setPageSelection}/>
            &nbsp;
            <PageButton text="<" currentPage={page} targetPage={Math.max(page - 1, 1)} setPageSelection={setPageSelection}/>
            &nbsp;
            <PageButton text=">" currentPage={page} targetPage={Math.min(page + 1, totalPages)} setPageSelection={setPageSelection}/>
            &nbsp;
            <PageButton text=">>" currentPage={page} targetPage={totalPages} setPageSelection={setPageSelection}/>
        </div>

    </>
}

interface PageButtonProps {
    text: string,
    currentPage: number
    targetPage: number
    setPageSelection: (page: number) => void,
}

const PageButton = ({text, currentPage, targetPage, setPageSelection}: PageButtonProps) => {
    if (currentPage === targetPage) {
        return <span className="badge bg-secondary noSelect">{text}</span>
    }
    return <span className="badge bg-info cursorPointer noSelect" onClick={() => {
        setPageSelection(targetPage)
    }}>{text}</span>
}

