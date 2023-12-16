package ro.ubbcluj.map.socialnetworkfx.utility;

public abstract class Pageable<E> {
    // Number of items selected. Default value (until specified) = -1;
    protected int noItemsPerPage = -1;
    // Current page. Default value (until specified) = 0;
    protected int currentPage = 0;

    public int getNoItems() {
        return noItemsPerPage;
    }

    public void setNoItems(int noItemsPerPage) {
        this.noItemsPerPage = noItemsPerPage;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    /**
     *
     * @return Items of the current page, limited by noOfItemsPerPage.
     */
    public abstract Iterable<E> getItemsOnPage();
}
