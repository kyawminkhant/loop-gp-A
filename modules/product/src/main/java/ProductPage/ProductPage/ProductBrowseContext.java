package ProductPage.ProductPage;

/** Selects whether the next customer catalogue is standard or personalized. */
final class ProductBrowseContext {

    private static boolean personalized;

    private ProductBrowseContext() { }

    static void useStandardCatalogue() {
        personalized = false;
    }

    static void usePersonalizedCatalogue() {
        personalized = true;
    }

    static boolean isPersonalized() {
        return personalized;
    }
}
