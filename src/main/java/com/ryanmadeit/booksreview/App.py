import streamlit as st
import requests
import urllib.parse as up

# Backend base URL
BACKEND = "http://thebooksite.onrender.com"

st.set_page_config(page_title="Find Books", page_icon="📚")

# -------------------------------
# Networking helpers
# -------------------------------
def fetch_json(url, params):
    try:
        r = requests.get(url, params=params, timeout=12)
        if r.status_code == 404 or r.status_code == 204 or not r.content:
            return None
        r.raise_for_status()
        return r.json()
    except requests.RequestException as e:
        st.error(f"Request failed: {e}")
        return None

def fetch_description(kind: str, params):
    # kind: "book" or "author"
    endpoint = f"{BACKEND}/ai/{kind}/describe"
    try:
        r = requests.get(endpoint, params=params, timeout=20)
        r.raise_for_status()
        data = r.json()
        return data.get("description")
    except requests.RequestException as e:
        st.error(f"Description request failed: {e}")
        return None

# -------------------------------
# Navigation helpers (same-tab)
# -------------------------------
def goto(view: str, **kwargs):
    st.query_params.clear()
    st.query_params["view"] = view
    for k, v in kwargs.items():
        st.query_params[k] = v
    st.rerun()  # rerun after changing URL state [web:25]

def back_to_search_button(label="⬅️ Back to search"):
    if st.button(label, use_container_width=False):
        goto("search")

# -------------------------------
# Card renderers
# -------------------------------
def render_book_card(book: dict):
    title = book.get("title", "Untitled")
    authors = book.get("authors", "Unknown")
    avg = book.get("averageRating") or book.get("average_rating") or "-"
    count = book.get("ratingsCount") or book.get("ratings_count") or "-"
    href = f"?view=book&title={up.quote(str(title))}"
    st.markdown(
        f"""
        <div style="border:1px solid #e6e6e6;border-radius:12px;padding:14px;margin-bottom:12px;
                    box-shadow:0 2px 6px rgba(0,0,0,0.15);">
            <h4 style="margin:0 0 8px 0;">{title}</h4>
            <div style="color:#555;margin-bottom:6px;">By {authors}</div>
            <div style="font-size:14px; color:#333;">⭐ {avg} • {count} ratings</div>
            <div style="margin-top:8px;">
                <a href="{href}" target="_self" style="text-decoration:none;">📖 View details</a>
            </div>
        </div>
        """,
        unsafe_allow_html=True,
    )

def render_author_card(book: dict):
    authors = book.get("authors", "Unknown")
    avg = book.get("averageRating") or book.get("average_rating") or "-"
    count = book.get("ratingsCount") or book.get("ratings_count") or "-"
    primary_author = (authors.split(",")[0].strip()
                      if isinstance(authors, str) else str(authors))
    href = f"?view=author&name={up.quote(primary_author)}"
    st.markdown(
        f"""
        <div style="border:1px solid #e6e6e6;border-radius:12px;padding:14px;margin-bottom:12px;
                    box-shadow:0 2px 6px rgba(0,0,0,0.15);">
            <h4 style="margin:0 0 8px 0;">{primary_author}</h4>
            <div style="font-size:14px; color:#333;">⭐ {avg} • {count} ratings</div>
            <div style="margin-top:8px;">
                <a href="{href}" target="_self" style="text-decoration:none;">👤 View author</a>
            </div>
        </div>
        """,
        unsafe_allow_html=True,
    )

# -------------------------------
# Pages
# -------------------------------
def page_search():
    st.title("Books Review and Recommender")

    # Title search
    tcol1, tcol2 = st.columns([3, 1], vertical_alignment="bottom")
    with tcol1:
        title_q = st.text_input("Search by title", placeholder="The Great Gatsby")
    with tcol2:
        search_title = st.button("Search Title", use_container_width=True)

    if search_title:
        data = fetch_json(f"{BACKEND}/books/get-title", {"title": title_q})
        if isinstance(data, dict):
            data = [data]
        if data:
            col1, col2, col3 = st.columns([3, 2, 2])
            cols = [col1, col2, col3]
            for i, book in enumerate(data):
                with cols[i % 3]:
                    render_book_card(book)

    # Author search
    acol1, acol2 = st.columns([4, 1], vertical_alignment="bottom")
    with acol1:
        authors_q = st.text_input("Search by author(s)", placeholder="J.K. Rowling")
    with acol2:
        search_author = st.button("Search Authors", use_container_width=True)

    if search_author:
        data = fetch_json(f"{BACKEND}/books/get-author", {"author": authors_q})
        if isinstance(data, dict):
            data = [data]
        if data:
            col1, col2, col3 = st.columns([4, 2, 2])
            cols = [col1, col2, col3]
            for i, book in enumerate(data):
                with cols[i % 3]:
                    render_author_card(book)

def page_book_details(title: str):
    st.header(f"Book: {title}")
    back_to_search_button()

    data = fetch_json(f"{BACKEND}/book/get-by-title", {"title": title})
    if not data:
        st.info("No details found for this title.")
        return

    st.subheader("About the book")
    st.write(f"Title: {data.get('title', '-')}")
    st.write(f"Authors: {data.get('authors', '-')}")
    st.write(f"Average rating: {data.get('averageRating') or data.get('average_rating') or '-'}")
    st.write(f"Ratings count: {data.get('ratingsCount') or data.get('ratings_count') or '-'}")
    st.write(f"Publisher: {data.get('publisher', '-')}")
    st.write(f"Published date: {data.get('publishedDate') or data.get('publication_date') or '-'}")

    # Generate description on demand
    if "desc_book" not in st.session_state:
        st.session_state["desc_book"] = {}
    if st.button("See Description", key=f"desc_book_btn_{title}"):
        with st.spinner("Generating description..."):
            desc = fetch_description("book", {"title": title})
        if desc:
            st.session_state["desc_book"][title] = desc
    if title in st.session_state["desc_book"]:
        st.write(st.session_state["desc_book"][title])

    st.subheader("About the author(s)")
    authors = data.get("authors") or "-"
    st.write(authors)

    if isinstance(authors, str) and authors.strip():
        primary_author = authors.split(",")[0].strip()
        st.subheader(f"More by {primary_author}")
        author_books = fetch_json(f"{BACKEND}/books/get-author", {"author": primary_author})
        if isinstance(author_books, dict):
            author_books = [author_books]
        if author_books:
            col1, col2, col3 = st.columns([3, 2, 2])
            cols = [col1, col2, col3]
            for i, book in enumerate(author_books):
                with cols[i % 3]:
                    render_book_card(book)

def page_author_details(name: str):
    st.header(f"Author: {name}")
    back_to_search_button()

    st.subheader("About the author")
    st.write(name)

    # Generate description on demand
    if "desc_author" not in st.session_state:
        st.session_state["desc_author"] = {}
    if st.button("See Description", key=f"desc_author_btn_{name}"):
        with st.spinner("Generating description..."):
            desc = fetch_description("author", {"name": name})
        if desc:
            st.session_state["desc_author"][name] = desc
    if name in st.session_state["desc_author"]:
        st.write(st.session_state["desc_author"][name])

    st.subheader(f"Books by {name}")
    data = fetch_json(f"{BACKEND}/books/get-author", {"author": name})
    if not data:
        st.info("No books found for this author.")
        return
    if isinstance(data, dict):
        data = [data]
    col1, col2, col3 = st.columns([3, 2, 2])
    cols = [col1, col2, col3]
    for i, book in enumerate(data):
        with cols[i % 3]:
            render_book_card(book)

    if data:
        sample_title = data[0].get("title")
        if sample_title:
            st.subheader("Other books")
            others = fetch_json(f"{BACKEND}/books/get-title", {"title": sample_title})
            if isinstance(others, dict):
                others = [others]
            if others:
                col1, col2, col3 = st.columns([3, 2, 2])
                cols = [col1, col2, col3]
                for i, book in enumerate(others):
                    with cols[i % 3]:
                        render_book_card(book)

# -------------------------------
# Router via query params
# -------------------------------
view = st.query_params.get("view", "search")  # [web:1]

if view == "search":
    page_search()
elif view == "book":
    title_param = st.query_params.get("title")
    if title_param:
        page_book_details(title_param)
    else:
        st.error("Missing book title.")
elif view == "author":
    author_param = st.query_params.get("name") or st.query_params.get("author")
    if author_param:
        page_author_details(author_param)
    else:
        st.error("Missing author name.")
else:
    page_search()
