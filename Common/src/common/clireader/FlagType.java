package common.clireader;

/**
 * Defines the expected behaviour for flag arguments and separators.
 */
public enum FlagType
{
    /** Mandatory value required (e.g., -f value) */
    ARG_REQUIRED,
    /** Optional value permitted (e.g., -f [value]) */
    ARG_OPTIONAL,
    /** Mandatory value via separator (e.g., --file=data.txt) */
    SEP_REQUIRED,
    /** Optional value via separator (e.g., --file[=data.txt]) */
    SEP_OPTIONAL,
    /** Boolean flag with no associated value (e.g., --verbose) */
    ARG_BLANK
}